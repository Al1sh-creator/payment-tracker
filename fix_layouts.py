import os
import re

directory = r"c:\Users\ALISH\Desktop\payment tracker\app\src\main\res\layout"
for filename in os.listdir(directory):
    if filename.endswith(".xml"):
        filepath = os.path.join(directory, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        
        if "<layout" in content:
            continue
            
        # Extract the XML declaration
        xml_decl_match = re.search(r'<\?xml.*?\?>\s*', content)
        xml_decl = xml_decl_match.group(0) if xml_decl_match else '<?xml version="1.0" encoding="utf-8"?>\n'
        
        # Remove XML declaration from content
        if xml_decl_match:
            content = content[xml_decl_match.end():]
            
        # Find all xmlns definitions and remove them from their original locations
        xmlns_matches = re.findall(r'\s+xmlns:[a-zA-Z]+="[^"]+"', content)
        
        # We need a unique set of xmlns strings
        unique_xmlns = list(set(xmlns_matches))
        xmlns_string = "".join(unique_xmlns)
        
        for xmlns in unique_xmlns:
            content = content.replace(xmlns, "")
            
        new_content = xml_decl.strip() + f"\n<layout{xmlns_string}>\n\n" + content.strip() + "\n\n</layout>\n"
        
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Fixed {filename}")
