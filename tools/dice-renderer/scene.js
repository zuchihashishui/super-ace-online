import * as T from 'three';
import {renderDensity} from './quality.js';
import {RoundedBoxGeometry} from 'three/addons/geometries/RoundedBoxGeometry.js';
const colors=['YELLOW','WHITE','PINK','BLUE','RED','GREEN'],hex=['#ffe82e','#fff9e4','#f632bb','#219cfa','#f3481c','#65c70b'],symbols=['☀','◆','♥','●','★','♣'];
let quality='auto',autoScale=1,slowRounds=0,fastRounds=0,renderCount=0;
try{const saved=localStorage.getItem('cg-quality');if(['auto','high','battery'].includes(saved))quality=saved;}catch{}
const tempPosition=new T.Vector3(),tempRotation=new T.Quaternion();
function setQuality(value){if(!['auto','high','battery'].includes(value))return;quality=value;autoScale=1;slowRounds=fastRounds=0;try{localStorage.setItem('cg-quality',value)}catch{}document.body.dataset.cgQuality=value;resize();}
function samplePerformance(samples){
 if(quality!=='auto'||samples.length<30)return;
 const average=samples.reduce((a,b)=>a+b,0)/samples.length;
 slowRounds=average>23?slowRounds+1:0;fastRounds=average<18?fastRounds+1:0;
 if(slowRounds>=1){autoScale=Math.max(.5,autoScale-.15);slowRounds=0;resize();}
 else if(fastRounds>=3){autoScale=Math.min(1,autoScale+.1);fastRounds=0;resize();}
}
let renderWidth=0,renderHeight=0,renderRatio=0;
let renderer,scene,camera,host,worker,sequence=0,frame=0,active=null,last=null,dead=false;const waiting=new Map(),dice=[],materials=[];
function init(){if(renderer)return true;if(dead)return false;try{
 host=document.createElement('div');host.id='cgWebgl';host.setAttribute('role','img');document.querySelector('.cg-dice-tray').prepend(host);
 host.hidden=true;renderer=new T.WebGLRenderer({alpha:true,antialias:true,powerPreference:'low-power'});renderer.shadowMap.enabled=true;renderer.shadowMap.type=T.PCFSoftShadowMap;renderer.outputColorSpace=T.SRGBColorSpace;host.append(renderer.domElement);
 renderer.domElement.addEventListener('webglcontextlost',e=>{e.preventDefault();dead=true;host.hidden=true;document.getElementById('cgCase').classList.remove('webgl-dice');active?.finish(true)});
 scene=new T.Scene();camera=new T.PerspectiveCamera(39,1,.1,40);camera.position.set(0,9,10);camera.lookAt(0,1,0);
 scene.add(new T.HemisphereLight(0xffffff,0xa67545,2.4));const light=new T.DirectionalLight(0xfff4d9,3.5);light.position.set(-3,8,5);light.castShadow=true;light.shadow.mapSize.set(1024,1024);light.shadow.camera.left=-5;light.shadow.camera.right=5;light.shadow.camera.top=5;light.shadow.camera.bottom=-5;light.shadow.bias=-.001;scene.add(light);
 const floor=new T.Mesh(new T.PlaneGeometry(8,6),new T.ShadowMaterial({opacity:.23}));floor.rotation.x=-Math.PI/2;floor.receiveShadow=true;scene.add(floor);
 // Solid material colors stay sharp at any resolution; no low-resolution face texture.
 hex.forEach(color=>materials.push(new T.MeshStandardMaterial({color,roughness:.3,metalness:.02})));
 const rim=new T.DirectionalLight(0xdceeff,.8);rim.position.set(4,3,-3);scene.add(rim);
 const bodyGeo=new RoundedBoxGeometry(1,1,1,3,.09),faceGeo=new T.PlaneGeometry(.76,.76),ivory=new T.MeshStandardMaterial({color:0xfff5d5,roughness:.26});
 for(let i=0;i<3;i++){const group=new T.Group(),body=new T.Mesh(bodyGeo,ivory);body.castShadow=true;group.add(body);const faces=[];[[.501,0,0,0,Math.PI/2,0],[-.501,0,0,0,-Math.PI/2,0],[0,.501,0,-Math.PI/2,0,0],[0,-.501,0,Math.PI/2,0,0],[0,0,.501,0,0,0],[0,0,-.501,0,Math.PI,0]].forEach((v,j)=>{const m=new T.Mesh(faceGeo,materials[j]);m.position.set(...v.slice(0,3));m.rotation.set(...v.slice(3));group.add(m);faces.push(m)});scene.add(group);dice.push({group,faces});}
 new ResizeObserver(resize).observe(host);window.addEventListener('resize',resize);worker=new Worker('dice-physics-worker.js?v=38-webgl');worker.onmessage=({data})=>{const entry=waiting.get(data.id);if(entry){clearTimeout(entry.timer);waiting.delete(data.id);data.error?entry.reject(new Error(data.error)):entry.resolve(data)}};worker.onerror=()=>{for(const entry of waiting.values()){clearTimeout(entry.timer);entry.reject(new Error('Physics unavailable'))}waiting.clear()};resize();return true;
 }catch(e){dead=true;host?.remove();renderer?.dispose();renderer=null;return false}}
function resize(){if(!renderer||dead)return;const w=host.clientWidth,h=host.clientHeight;if(!w||!h)return;const base=renderDensity(w,h,innerWidth,innerHeight,devicePixelRatio,renderer.capabilities.maxTextureSize),ratio=quality==='battery'?Math.min(1.25,base):base*(quality==='auto'?autoScale:1);renderer.shadowMap.enabled=quality!=='battery';if(w!==renderWidth||h!==renderHeight||Math.abs(ratio-renderRatio)>.001){renderer.setPixelRatio(ratio);renderer.setSize(w,h);renderWidth=w;renderHeight=h;renderRatio=ratio;}camera.aspect=w/h;camera.zoom=Math.min(1,camera.aspect/.95);camera.updateProjectionMatrix();renderer.render(scene,camera);renderCount++}
function simulate(){const id=++sequence;return new Promise((resolve,reject)=>{const timer=setTimeout(()=>{waiting.delete(id);reject(new Error('Physics timeout'))},3000);waiting.set(id,{resolve,reject,timer});worker.postMessage({id,seed:crypto.getRandomValues(new Uint32Array(1))[0]})})}
function pose(sample,next=sample,f=0){dice.forEach((d,i)=>{const o=i*7;d.group.position.set(sample[o],sample[o+1],sample[o+2]).lerp(tempPosition.set(next[o],next[o+1],next[o+2]),f);d.group.quaternion.set(sample[o+3],sample[o+4],sample[o+5],sample[o+6]).slerp(tempRotation.set(next[o+3],next[o+4],next[o+5],next[o+6]),f)});renderer.render(scene,camera);renderCount++}
function stop(){cancelAnimationFrame(frame);if(active){const old=active;active=null;old.cancelled=true;old.resolve(false)}document.getElementById('cgCase')?.classList.remove('webgl-dice');if(host)host.hidden=true;}
async function roll(outcome,{instant=false,onImpact=()=>{}}={}){stop();last=null;if(!init()||dead)return false;let resolve;const completion=new Promise(r=>resolve=r),job={resolve,cancelled:false,finish:null};active=job;job.finish=failed=>{if(job.cancelled)return;cancelAnimationFrame(frame);if(failed){job.cancelled=true;host.hidden=true;document.getElementById('cgCase').classList.remove('webgl-dice');}else if(last)pose(last.frames.at(-1));active=null;resolve(!failed)};
 try{let result;for(let attempt=0;attempt<3;attempt++){try{result=await simulate();break}catch(e){if(attempt===2)throw e}}if(job.cancelled)return false;
 last=result;dice.forEach((d,i)=>{const selected=colors.indexOf(outcome.dice[i]),order=[0,1,2,3,4,5],top=result.top[i].index;[order[top],order[selected]]=[order[selected],order[top]];d.faces.forEach((face,j)=>face.material=materials[order[j]]);d.group.userData={top:outcome.dice[i],order};});
 host.hidden=false;document.getElementById('cgCase').classList.add('webgl-dice');host.setAttribute('aria-label',outcome.dice.join(' · '));resize();const duration=3000;let start,previous;const timings=[];let impact=false;
 const draw=now=>{if(job.cancelled)return;try{start??=now;if(previous&&now-previous<100)timings.push(now-previous);previous=now;const p=instant||job.instant||document.hidden?1:Math.min(1,(now-start)/duration),at=p*(result.frames.length-1),a=Math.floor(at);pose(result.frames[a],result.frames[Math.min(a+1,result.frames.length-1)],at-a);if(!impact&&p>.2){impact=true;onImpact()}if(p===1){job.finish(false);if(!instant&&!document.hidden)samplePerformance(timings);}else frame=requestAnimationFrame(draw)}catch(e){job.finish(true)}};frame=requestAnimationFrame(draw);
 }catch(e){if(!job.cancelled){active=null;host.hidden=true;document.getElementById('cgCase').classList.remove('webgl-dice');resolve(false)}}return completion;}
function finish(){if(active){active.instant=true;if(last){try{active.finish(false)}catch(e){active?.finish(true)}}}}
window.CG3D={roll,stop,finish,setQuality,prepare:()=>init(),debug:()=>({available:!!renderer&&!dead,quality,autoScale,renderCount,pixelRatio:renderRatio,bufferWidth:renderer?.domElement.width,bufferHeight:renderer?.domElement.height,cssWidth:renderWidth,cssHeight:renderHeight,active:!!active,contacts:last?.contacts,frames:last?.frames.length,top:last?.top,colors:dice.map(d=>{let best=-2,index=0;[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].forEach((n,i)=>{const y=new T.Vector3(...n).applyQuaternion(d.group.quaternion).y;if(y>best){best=y;index=i}});return colors[materials.indexOf(d.faces[index].material)]}),positions:dice.map(d=>d.group.position.toArray()),quaternions:dice.map(d=>d.group.quaternion.toArray())})};
