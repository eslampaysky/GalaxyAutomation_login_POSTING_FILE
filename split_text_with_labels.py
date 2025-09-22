import sys
import os

# ensure stdout uses utf-8 (works on modern Python)
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# ---- Config ----
BASE_DIR = r"C:\Users\EslamSamy\IdeaProjects\Galaxy_Auto_Login_Test\extracted"
OUTPUT_FILE = "formatted_output.txt"

# ---- Normalize extracted files first ----
for fname in os.listdir(BASE_DIR):
    if fname.startswith("POSTFLIN."):
        timestamp = fname.split("POSTFLIN.")[-1]
        new_name = f"{timestamp}.txt"
        old_path = os.path.join(BASE_DIR, fname)
        new_path = os.path.join(BASE_DIR, new_name)
        # Always overwrite
        os.replace(old_path, new_path)
        print(f"[PYTHON] Renamed {fname} → {new_name}")

# ---- Input arg: timestamp or filename fragment ----
if len(sys.argv) < 2:
    print(" Usage: python split_text_with_labels.py <timestamp_or_filename_fragment>")
    sys.exit(2)

fragment = sys.argv[1]
print(f"[PYTHON] Processing: looking for files containing {fragment!r} in {BASE_DIR}")

# Try to find candidate file
candidates = []
try:
    for fname in os.listdir(BASE_DIR):
        if fragment in fname:
            candidates.append(fname)
except FileNotFoundError:
    print(f"[PYTHON] Extracted directory not found: {BASE_DIR}")
    sys.exit(3)

if not candidates:
    print(f"[PYTHON] No file found in {BASE_DIR} containing: {fragment}")
    print("Files present:", os.listdir(BASE_DIR))
    sys.exit(4)

# Pick first candidate
chosen_name = candidates[0]
input_path = os.path.join(BASE_DIR, chosen_name)
print(f"[PYTHON] Using file: {chosen_name}")
print(f"[PYTHON] Full path: {input_path}")

file_size = os.path.getsize(input_path)
print(f"[PYTHON] Size detected: {file_size} bytes")
if file_size == 0:
    print(f"[WARNING] The file {chosen_name} is empty (size=0). Continuing anyway...")

# Try to open in utf-8, fallback to latin-1
def read_lines(path):
    try:
        with open(path, "r", encoding="utf-8") as f:
            return [line.rstrip("\n") for line in f if line.strip()]
    except UnicodeDecodeError:
        with open(path, "r", encoding="latin-1") as f:
            return [line.rstrip("\n") for line in f if line.strip()]

# === parsing rules ===
split_rules = {
    "HR": [2, 8, 6, 16, 14, 1],
    "HS": [2, 8, 15, 15, 15, 8, 8, 14, 3, 1],
    "DT": [2, 8, 7, 1, 8, 22, 6, 6, 1, 1, 12, 4, 2, 14, 18, 1, 3, 1, 2, 18, 6, 3, 1],
    "EC": [2, 8, 8, 50, 1, 3, 5, 3, 1],
    "TS": [2, 8, 15, 15, 15, 8, 8, 14, 6, 18, 6, 18],
    "TR": [2, 8, 6, 6, 8, 8, 18, 18],
}

field_names = {
    "DT": [
        "Record type", "Record sequence", "Transaction sequence", "Service type", "Voucher number",
        "Card number", "Expiry date", "Processing code", "Reversal flag", "Authorization flag",
        "POS data", "POS entry mode", "POS condition code", "Transaction date & time",
        "Transaction amount", "Transaction sign", "Transaction currency", "Currency exponent",
        "Reversal reason code", "Replacement amounts", "Authorization code", "Service code",
        "Single message indicator"
    ],
    "EC": [
        "Record type", "Record sequence", "Voucher number", "Purchase identifier",
        "E-commerce indicator", "Security level indicator", "Agent Unique ID",
        "Wallet program data", "Deferred billing indicator"
    ],
    "TS": [
        "Record type", "Record sequence", "Merchant number", "Outlet number", "Terminal ID",
        "Batch number", "Batch capture date", "Batch date & time", "Records count debit",
        "Net amount debit", "Records count credit", "Net amount credit"
    ],
    "TR": [
        "Record type", "Record sequence", "Institution ID", "File sender",
        "Records count debit", "Record count credit", "Net amount credit", "Net amount debit"
    ],
}

# === Read file lines ===
lines = read_lines(input_path)

output_lines = []
for line_num, line in enumerate(lines, 1):
    record_type = line[:2]
    if record_type not in split_rules:
        print(f"[PYTHON] Line {line_num}: Unknown record type '{record_type}' — skipped.")
        continue

    split_lengths = split_rules[record_type]
    expected_length = sum(split_lengths)

    if len(line) < expected_length:
        print(f"[PYTHON] Line {line_num}: Too short for type '{record_type}' "
              f"(expected {expected_length}, got {len(line)}) — skipped.")
        continue

    parts = []
    index = 0
    for length in split_lengths:
        parts.append(line[index:index + length])
        index += length

    labels = field_names.get(record_type)
    output_lines.append(f"# Record Type: {record_type}")

    if labels and len(labels) == len(parts):
        for label, value in zip(labels, parts):
            output_lines.append(f"{label}: {value}")
    else:
        output_lines.extend(parts)

    output_lines.append("")

# Write output
with open(OUTPUT_FILE, "w", encoding="utf-8") as outfile:
    outfile.write("\n".join(output_lines))

print(f"[PYTHON] Done! Output written to '{OUTPUT_FILE}' (source file: {chosen_name})")
sys.exit(0)
