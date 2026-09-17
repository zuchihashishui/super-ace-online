"""Create a verified versioned ZIP after building and testing the release JAR."""
import pathlib,zipfile,hashlib,sys,tempfile
root=pathlib.Path(__file__).resolve().parents[1]
output=pathlib.Path(sys.argv[1]).resolve()
jar=root/'release/super-ace-online-13.0.0.jar'
assert hashlib.sha256(jar.read_bytes()).hexdigest() in (root/'release/SHA256SUMS.txt').read_text()
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for file in (root/'client').rglob('*'):
  if file.is_file() and file.suffix!='.md':assert z.read('BOOT-INF/classes/static/'+file.relative_to(root/'client').as_posix())==file.read_bytes(),str(file)
with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(root.rglob('*')):
  if p.is_file() and not any(x in {'target','node_modules','__pycache__','.git'} for x in p.relative_to(root).parts) and p.name not in {'.env','.env.local','pom.verify.xml'} and p.suffix!='.pyc':z.write(p,pathlib.Path(root.name)/p.relative_to(root))
with zipfile.ZipFile(output) as z:
 assert z.testzip() is None
 with tempfile.TemporaryDirectory(prefix='ace-release-') as d:z.extractall(d)
print(output,output.stat().st_size,'bytes; CRC, extraction, JAR checksum and client parity OK')
