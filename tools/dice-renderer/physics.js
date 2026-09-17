import * as C from 'cannon-es';
const normals=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]];
self.onmessage=({data:{id,seed}})=>{try{
 let n=seed>>>0;const rand=()=>{n=(Math.imul(n,1664525)+1013904223)>>>0;return n/4294967296;};
 const world=new C.World({gravity:new C.Vec3(0,-24,0),allowSleep:true});world.solver.iterations=14;
 const material=new C.Material();world.addContactMaterial(new C.ContactMaterial(material,material,{friction:.48,restitution:.28}));
 function wall(x,y,z,a,b,c){const body=new C.Body({mass:0,material,shape:new C.Box(new C.Vec3(a,b,c))});body.position.set(x,y,z);world.addBody(body)}
 wall(0,-.3,0,4,.3,3);wall(-3.7,2,0,.2,2,3);wall(3.7,2,0,.2,2,3);wall(0,2,-2.6,4,2,.2);wall(0,2,2.6,4,2,.2);
 let contacts=0;const bodies=[0,1,2].map(i=>{const b=new C.Body({mass:1,material,shape:new C.Box(new C.Vec3(.5,.5,.5)),linearDamping:.2,angularDamping:.28,sleepSpeedLimit:.08,sleepTimeLimit:.4});b.position.set((i-1)*1.25,3.9+i*.42,-1.15+i*.22);b.velocity.set((1-i)*1.65+(rand()-.5)*.5,-.8,2+rand());b.angularVelocity.set(6+rand()*7,(rand()-.5)*14,(rand()-.5)*16);b.quaternion.setFromEuler(rand()*3,rand()*3,rand()*3);b.addEventListener('collide',()=>contacts++);world.addBody(b);return b;});
 const frames=[];let resting=0;
 for(let step=0;step<1200;step++){world.step(1/120);if(step%2===0)frames.push(bodies.flatMap(b=>[b.position.x,b.position.y,b.position.z,b.quaternion.x,b.quaternion.y,b.quaternion.z,b.quaternion.w]));const stable=bodies.every(b=>b.velocity.length()<.05&&b.angularVelocity.length()<.05);resting=stable?resting+1:0;if(step>240&&resting>35)break;}
 const top=bodies.map(b=>{let best=-2,index=-1;normals.forEach((v,i)=>{const y=b.quaternion.vmult(new C.Vec3(...v)).y;if(y>best){best=y;index=i}});return {index,alignment:best};});
 if(top.some(t=>t.alignment<.995)||bodies.some(b=>b.velocity.length()>.1))throw new Error('Dice did not settle flat');
 self.postMessage({id,frames,top,contacts});
 }catch(e){self.postMessage({id,error:e.message})}};
