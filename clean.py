import re

with open('database/revitafisio_ddl.sql', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    stripped = line.strip()
    if stripped.startswith('--'):
        # Keep section headers
        if re.match(r'^-- \d+\.', stripped) or stripped == '-- =====================================================================':
            new_lines.append(line)
        elif stripped == '-- FIM DO SCRIPT':
            new_lines.append(line)
        elif 'RevitaFisio - Sistema Clinico ETB 2025' in line or 'Script DDL' in line:
            new_lines.append(line)
    else:
        new_lines.append(line)

with open('database/revitafisio_ddl.sql', 'w') as f:
    f.writelines(new_lines)

