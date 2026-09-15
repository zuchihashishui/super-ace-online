"""Repackage compiled Java 21 classes and current resources using intact dependencies.
Usage: python3 tools/build_release.py /absolute/path/to/compiled/classes
For a standard build use: cd server && mvn clean package
"""
import sys, pathlib, zipfile, io, hashlib
root=pathlib.Path(__file__).resolve().parents[1]
classes=pathlib.Path(sys.argv[1]).resolve()
assert (classes/'vn/emerald/ace/LobbyGameService.class').is_file(), 'Compile all Java sources first'
jar=root/'release/super-ace-online-13.0.0.jar'
original=jar.read_bytes()
updates={}
for base,prefix in [(classes,'BOOT-INF/classes/'),(root/'server/src/main/resources','BOOT-INF/classes/'),(root/'client','BOOT-INF/classes/static/')]:
 for p in base.rglob('*'):
  if p.is_file() and p.suffix!='.md' and p.name not in {'ModeIntegration.class','Simulate.class'}:updates[prefix+p.relative_to(base).as_posix()]=p.read_bytes()
with zipfile.ZipFile(io.BytesIO(original)) as old,zipfile.ZipFile(jar,'w') as out:
 assert old.testzip() is None
 for info in old.infolist():
  # Drop obsolete compiled inner classes when replacing the whole package.
  if info.filename.startswith('BOOT-INF/classes/vn/emerald/ace/') and info.filename.endswith('.class') and info.filename not in updates:continue
  out.writestr(info,updates.pop(info.filename,old.read(info)))
 for name,data in updates.items():out.writestr(name,data,compress_type=zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for name in z.namelist():
  if name.startswith('BOOT-INF/lib/') and name.endswith('.jar'):
   assert z.getinfo(name).compress_type==zipfile.ZIP_STORED
   with zipfile.ZipFile(io.BytesIO(z.read(name))) as dep:assert dep.testzip() is None
(root/'release/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  '+jar.name+'\n')
output=root.parents[1]/'super-ace-online-java21.zip'
with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as out:
 for p in sorted(root.rglob('*')):
  if p.is_file() and not any(x in {'target','node_modules','__pycache__','.git'} for x in p.relative_to(root).parts) and p.name not in {'.env','.env.local'} and p.suffix!='.pyc':out.write(p,pathlib.Path(root.name)/p.relative_to(root))
with zipfile.ZipFile(output) as z:assert z.testzip() is None
print('Verified release and ZIP:',output,output.stat().st_size)
