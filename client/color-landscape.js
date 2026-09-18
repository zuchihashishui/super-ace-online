'use strict';
// Native fullscreen is requested inside the entry click; CSS remains the fallback.
Object.assign(filipino,{'Games':'Mga laro','Full screen':'Buong screen','Turn your phone sideways':'Ihiga ang iyong telepono'});
let cgOwnFullscreen=false,cgOwnOrientation=false,cgFullscreenPending=null,cgOrientationVersion=0;
function cgPhone(){return matchMedia('(pointer: coarse)').matches&&Math.min(innerWidth,innerHeight)<=600&&Math.max(innerWidth,innerHeight)<=1200;}
function cgUnlockOrientation(){cgOrientationVersion++;if(cgOwnOrientation){try{screen.orientation.unlock()}catch{}cgOwnOrientation=false;}}
function cgScreenLayout(){
 const inGame=activeView==='colorGameView',on=inGame&&cgPhone(),rotated=on&&innerHeight>innerWidth;
 document.body.classList.toggle('cg-immersive',inGame);document.body.classList.toggle('cg-wide',on);document.body.classList.toggle('cg-rotated',rotated);
 document.documentElement.style.setProperty('--cg-screen-w',(rotated?innerHeight:innerWidth)+'px');document.documentElement.style.setProperty('--cg-screen-h',(rotated?innerWidth:innerHeight)+'px');
 if(!inGame){cgUnlockOrientation();if(cgOwnFullscreen){cgOwnFullscreen=false;if(document.fullscreenElement)void document.exitFullscreen().catch(()=>{});}}
}
function cgEnterFullscreen(){
 if(activeView!=='colorGameView')return Promise.resolve();
 if(cgFullscreenPending)return cgFullscreenPending;
 // Call before the first await: browsers require the original tap/click activation.
 let request;try{request=!document.fullscreenElement&&document.documentElement.requestFullscreen?document.documentElement.requestFullscreen():null;}catch{cgScreenLayout();return Promise.resolve();}
 const operation=(async()=>{
  try{
   if(request){await request;cgOwnFullscreen=true;}
   if(activeView==='colorGameView'&&document.fullscreenElement&&cgPhone()&&screen.orientation?.lock){
    const version=++cgOrientationVersion;await screen.orientation.lock('landscape');
    if(version===cgOrientationVersion&&activeView==='colorGameView'&&document.fullscreenElement)cgOwnOrientation=true;
    else{try{screen.orientation.unlock()}catch{}}
   }
  }catch{ /* Unsupported/rejected fullscreen or orientation lock: keep CSS landscape. */ }
  finally{cgScreenLayout();}
 })();
 cgFullscreenPending=operation;void operation.finally(()=>{if(cgFullscreenPending===operation)cgFullscreenPending=null});return operation;
}
const cgScreenBaseShow=showView;showView=async function(id){
 const previous=activeView,result=cgScreenBaseShow(id);cgScreenLayout();
 if(previous!=='colorGameView'&&id==='colorGameView'&&activeView===id&&navigator.userActivation?.isActive)void cgEnterFullscreen();
 await result;cgScreenLayout();
};
$('cgExit').onclick=()=>showView('gameView');$('cgFullscreen').onclick=()=>cgEnterFullscreen();
addEventListener('resize',cgScreenLayout);screen.orientation?.addEventListener('change',cgScreenLayout);window.visualViewport?.addEventListener('resize',cgScreenLayout);document.addEventListener('fullscreenchange',()=>{if(!document.fullscreenElement){cgOwnFullscreen=false;cgUnlockOrientation();}cgScreenLayout();});
cgScreenLayout();
