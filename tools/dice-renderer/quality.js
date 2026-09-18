// The scene occupies only part of the viewport. Use a 720px short-edge screen
// density target, not a stretched 1280x720 canvas, and bound GPU allocations.
export function renderDensity(width,height,viewportWidth,viewportHeight,dpr,maxDimension=4096){
 const shortEdge=Math.max(1,Math.min(viewportWidth,viewportHeight));
 const wanted=Math.min(3,Math.max(1,Number.isFinite(dpr)?dpr:1,720/shortEdge));
 return Math.min(wanted,Math.sqrt(1500000/Math.max(1,width*height)),maxDimension/Math.max(1,width,height));
}
