"""Refresh static client resources in the existing release JAR, preserving backend bytes."""
import hashlib, io, pathlib, zipfile
root = pathlib.Path(__file__).resolve().parents[1]
jar = root / 'release/super-ace-online-13.0.0.jar'
original = jar.read_bytes()
with zipfile.ZipFile(io.BytesIO(original)) as old, zipfile.ZipFile(jar, 'w') as out:
    assert old.testzip() is None
    for info in old.infolist():
        if not info.filename.startswith('BOOT-INF/classes/static/'):
            out.writestr(info, old.read(info))
    out.writestr('BOOT-INF/classes/static/', b'')
    for directory in sorted((root / 'client').rglob('*')):
        if directory.is_dir():
            out.writestr('BOOT-INF/classes/static/' + directory.relative_to(root / 'client').as_posix() + '/', b'')
    for file in sorted((root / 'client').rglob('*')):
        if file.is_file() and file.suffix != '.md':
            out.write(file, 'BOOT-INF/classes/static/' + file.relative_to(root / 'client').as_posix())
with zipfile.ZipFile(io.BytesIO(original)) as old, zipfile.ZipFile(jar) as new:
    assert new.testzip() is None
    for name in old.namelist():
        if not name.startswith('BOOT-INF/classes/static/'):
            assert old.read(name) == new.read(name), name
    for info in new.infolist():
        if info.filename.startswith('BOOT-INF/lib/') and info.filename.endswith('.jar'):
            assert info.compress_type == zipfile.ZIP_STORED
(root / 'release/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest() + '  ' + jar.name + '\n')
print('Client updated; all backend classes, dependencies and migrations preserved byte-for-byte.')
