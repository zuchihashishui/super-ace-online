"""Package frontend changes into the intact release JAR, preserving nested JAR storage."""
import zipfile, pathlib, io, hashlib
root=pathlib.Path(__file__).resolve().parents[1]
jar=root/'release/super-ace-online-13.0.0.jar'
source=jar.read_bytes()
assets={f'BOOT-INF/classes/static/{p.relative_to(root/"client")}':p.read_bytes() for p in (root/'client').rglob('*') if p.is_file() and p.suffix!='.md'}
assets['BOOT-INF/classes/db/migration/V4__lucky_seven_club.sql']=(root/'server/src/main/resources/db/migration/V4__lucky_seven_club.sql').read_bytes()
with zipfile.ZipFile(io.BytesIO(source)) as old,zipfile.ZipFile(jar,'w') as out:
 assert old.testzip() is None
 for info in old.infolist():out.writestr(info,assets.pop(info.filename,old.read(info)))
 for name,data in assets.items():out.writestr(name,data,compress_type=zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for name in z.namelist():
  if name.endswith('.jar'):
   assert z.getinfo(name).compress_type==zipfile.ZIP_STORED
   with zipfile.ZipFile(io.BytesIO(z.read(name))) as dependency:assert dependency.testzip() is None
(root/'release/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  '+jar.name+'\n')
output=root.parents[1]/'super-ace-online-java21.zip'
with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as out:
 for p in sorted(root.rglob('*')):
  if p.is_file() and not any(x in {'target','node_modules','__pycache__','.git'} for x in p.relative_to(root).parts) and p.name not in {'.env','.env.local'} and p.suffix!='.pyc':out.write(p,str(pathlib.Path(root.name)/p.relative_to(root)))
with zipfile.ZipFile(output) as z:assert z.testzip() is None
print('Verified release, nested dependencies and ZIP:',output,output.stat().st_size)
