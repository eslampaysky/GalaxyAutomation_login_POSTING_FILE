import pyodbc
import sys
import os
from datetime import datetime

# --- DB connection settings ---
DB_SERVER = "az-db-gim-uat.database.windows.net"
DB_NAME = "Cube2"
DB_USER = "gimpay"
DB_PASSWORD = "P@$wwn2u!@2"
DB_DRIVER = "{ODBC Driver 17 for SQL Server}"

# --- Directory where posting (extracted) files are saved ---
EXTRACTED_DIR = r"C:\Users\EslamSamy\IdeaProjects\Galaxy_Auto_Login_Test\extracted"
REPORT_FILE = "validation_report.txt"

# --- Safe print wrapper ---
def safe_print(msg):
    try:
        print(msg)
    except UnicodeEncodeError:
        print(msg.encode("ascii", "ignore").decode())

# --- Establish DB connection ---
def connect_db():
    conn_str = (
        f"DRIVER={DB_DRIVER};"
        f"SERVER={DB_SERVER};"
        f"DATABASE={DB_NAME};"
        f"UID={DB_USER};"
        f"PWD={DB_PASSWORD}"
    )
    try:
        return pyodbc.connect(conn_str, timeout=10)
    except Exception as e:
        safe_print(f"❌ DB connection failed: {e}")
        sys.exit(1)

# --- Fetch settlement rows from DB ---
def fetch_settlement_rows(date_from, date_to):
    conn = connect_db()
    cursor = conn.cursor()

    sql = """
    SELECT *
    FROM dbo.SettlementData
    WHERE IsReady = 1
      AND BankId = ?
      AND IssuingDate >= ?
      AND IssuingDate < ?
    ORDER BY IssuingDate;
    """
    cursor.execute(sql, ("10124", date_from, date_to))

    cols = [c[0] for c in cursor.description]
    rows = [dict(zip(cols, r)) for r in cursor.fetchall()]

    conn.close()
    return rows

# --- DT field mapping (Table B, voucher number 12 chars) ---
DT_FIELDS = [
    ("Record type", 1, 2),
    ("Record sequence", 3, 8),
    ("Transaction sequence", 11, 7),
    ("Service type", 18, 1),
    ("Voucher number", 19, 8),
    ("Card number", 27, 22),
    ("Expiry date", 49, 6),
    ("Processing code", 55, 6),
    ("Reversal flag", 61, 1),
    ("Authorization flag", 62, 1),
    ("POS data", 63, 12),
    ("POS entry mode", 75, 4),
    ("POS condition code", 79, 2),
    ("Transaction date & time", 81, 14),
    ("Transaction amount", 95, 18),
    ("Transaction sign", 113, 1),
    ("Transaction currency", 114, 3),
    ("Currency exponent", 117, 1),
    ("Reversal reason code", 118, 2),
    ("Replacement amounts", 120, 18),
    ("Authorization code", 138, 6),
    ("Service code", 144, 3),
    ("Single message indicator", 147, 1),
]

# --- Parse one DT record by fixed positions ---
def parse_dt_record(record):
    parsed = {}
    for field, pos, length in DT_FIELDS:
        parsed[field] = record[pos - 1: pos - 1 + length].strip()
    return parsed

# --- Build expected values from DB row ---
def expected_file_values(row):
    db_rrn = str(row.get("RETRIEVAL_REFERENCE_NUMBER", "")).strip()
    voucher = db_rrn[-6:].zfill(6)
    expiry = str(row["CardholderCardNumberExpiryDate"])
    if len(expiry) == 4:
        expiry = "20" + expiry

    values = {
        "Card number": str(row["CardholderCardNumberInvolvedInTransaction"]),
        "Voucher number": voucher,
        "Expiry date": expiry,
        "Transaction amount": str(row["SOURCE_AMOUNT"]).rjust(18, "0"),
        "Transaction currency": str(row["SOURCE_CURRENCY_CODE"]),
        "POS entry mode": str(row["POSEntryMode"]).rjust(4),
        "POS condition code": str(row["POSConditionCode"]).rjust(2),
        "POS data": str(row["POS_DATA"]),
    }

    if isinstance(row["IssuingDate"], datetime):
        values["Transaction date & time"] = row["IssuingDate"].strftime("%Y%m%d%H%M%S")

    # Processing code & sign
    if row["TrnxType"] == 0:  # Debit
        values["Processing code"] = "000000"
        values["Transaction sign"] = "C"
    elif row["TrnxType"] == 1:  # Credit
        values["Processing code"] = "200000"
        values["Transaction sign"] = "D"

    return values

# --- Parse posting file into records (each DT line) ---
def parse_file_records(file_path):
    records = []
    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line.startswith("DT"):
                records.append(line)
    return records

# --- Find matching DT record using first 6 digits of voucher ---
def find_matching_record(db_row, records):
    db_rrn = str(db_row.get("RETRIEVAL_REFERENCE_NUMBER", "")).strip()
    db_rrn6 = db_rrn[-6:].zfill(6)  # take last 6 digits

    for rec in records:
        parsed = parse_dt_record(rec)
        file_voucher = parsed.get("Voucher number", "").strip()
        file_voucher6 = file_voucher[:6].zfill(6)

        if db_rrn6 == file_voucher6:
            return parsed, rec, file_voucher

    return None, None, None

# --- Detailed validation & reporting ---
def validate_and_report(db_rows, records, report_path):
    total_fails = 0
    with open(report_path, "w", encoding="utf-8") as report:
        report.write("=== Validation Report (Row ↔ DT Matching) ===\n\n")

        for idx, row in enumerate(db_rows, start=1):
            db_rrn = str(row.get("RETRIEVAL_REFERENCE_NUMBER", "")).strip()
            voucher_id = db_rrn[:6].zfill(6)

            report.write(f"--- DB Row {idx} (Voucher {voucher_id}) ---\n")

            parsed_record, raw_record, full_file_voucher = find_matching_record(row, records)
            if not parsed_record:
                msg = f"[FAIL] No matching DT record found for Voucher {voucher_id}"
                report.write(msg + "\n\n")
                safe_print(msg)
                total_fails += 1
                continue

            expected_values = expected_file_values(row)

            for field, expected in expected_values.items():
                actual = parsed_record.get(field, "")
                if actual == expected:
                    msg = f"[PASS] {field}: DB='{expected}' == File='{actual}'"
                else:
                    msg = f"[FAIL] {field}: DB='{expected}' != File='{actual}'"
                    total_fails += 1
                report.write(msg + "\n")
                safe_print(msg)

            report.write(f"Raw DT record: {raw_record}\n")
            report.write(f"Full file voucher: {full_file_voucher}\n\n")

    safe_print(f"📄 Detailed validation report written to {report_path}")
    return total_fails

# --- Main program ---
def main():
    if len(sys.argv) < 4:
        safe_print("Usage: python validate_with_db.py <timestamp> <date_from> <date_to>")
        sys.exit(2)

    timestamp, date_from, date_to = sys.argv[1:4]
    file_path = os.path.join(EXTRACTED_DIR, f"{timestamp}.txt")

    if not os.path.exists(file_path):
        safe_print(f"❌ Extracted file not found: {file_path}")
        sys.exit(3)

    records = parse_file_records(file_path)
    db_rows = fetch_settlement_rows(date_from, date_to)
    safe_print(f"[PYTHON] Found {len(db_rows)} rows in DB between {date_from} and {date_to}")

    total_fails = validate_and_report(db_rows, records, REPORT_FILE)

    safe_print(f"=== SUMMARY: {total_fails} FAIL(s) found ===")

    if total_fails > 0:
        sys.exit(4)  # non-zero -> TestNG will mark test failed
    else:
        safe_print("✅ DB validation completed with all PASS")
        sys.exit(0)

if __name__ == "__main__":
    main()
