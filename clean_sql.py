import re

with open('database/revitafisio_ddl.sql', 'r') as f:
    lines = f.readlines()

cleaned_lines = []
skip_mode = False

for line in lines:
    # Skip the massive blocks at the beginning and end
    if 'REVISADO E VALIDADO CONTRA O CODIGO REAL' in line or 'IMPORTANTE - NAO ENCONTREI' in line or 'COMO USAR NO DBEAVER' in line or 'PROXIMO PASSO OBRIGATORIO' in line:
        continue
    if line.startswith('-- CONFIRMADO no codigo') or line.startswith('-- CORRIGIDO vs. diagrama') or line.startswith('-- ATENCAO:') or line.startswith('-- NOME DA TABELA CORRIGIDO') or line.startswith('-- CORRECOES CRITICAS vs.'):
        skip_mode = True
        continue
    
    if skip_mode:
        if line.startswith('--') and not line.startswith('-- =====================================================================') and not line.startswith('-- 1.') and not line.startswith('-- 2.'):
            continue
        else:
            skip_mode = False

    cleaned_lines.append(line)

# Clean up header block manually
header = """-- =====================================================================
-- RevitaFisio - Sistema Clinico ETB 2025
-- Script DDL (MySQL 8.x / MariaDB)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS revitafisio
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE revitafisio;

SET FOREIGN_KEY_CHECKS = 0;
"""

# Let's just do a simpler filtering
new_content = []
in_footer = False
for line in cleaned_lines:
    if 'FIM DO SCRIPT' in line:
        in_footer = True
        new_content.append(line)
        new_content.append('-- =====================================================================\n')
        break
    
    if line.startswith('-- =====================================================================') or line.startswith('-- 1.') or line.startswith('-- 2.') or line.startswith('-- 3.') or line.startswith('-- 4.') or line.startswith('-- 5.') or line.startswith('-- 6.') or line.startswith('-- 7.') or line.startswith('-- 8.') or line.startswith('-- 9.') or line.startswith('-- 10.') or line.startswith('-- 11.') or line.startswith('-- 12.') or line.startswith('-- 13.') or line.startswith('-- 14.') or line.startswith('-- 15.') or not line.startswith('--'):
        new_content.append(line)

with open('database/revitafisio_ddl.sql', 'w') as f:
    f.write(header)
    # Start after the USE revitafisio and SET FOREIGN KEY part
    start_writing = False
    for line in new_content:
        if 'DROP TABLE' in line:
            start_writing = True
        if start_writing or line.startswith('-- ====================================================================='):
            f.write(line)

