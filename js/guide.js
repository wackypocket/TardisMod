var _____WB$wombat$assign$function_____=function(name){return (self._wb_wombat && self._wb_wombat.local_init && self._wb_wombat.local_init(name))||self[name];};if(!self.__WB_pmw){self.__WB_pmw=function(obj){this.__WB_source=obj;return this;}}{
let window = _____WB$wombat$assign$function_____("window");
let self = _____WB$wombat$assign$function_____("self");
let document = _____WB$wombat$assign$function_____("document");
let location = _____WB$wombat$assign$function_____("location");
let top = _____WB$wombat$assign$function_____("top");
let parent = _____WB$wombat$assign$function_____("parent");
let frames = _____WB$wombat$assign$function_____("frames");
let opens = _____WB$wombat$assign$function_____("opens");
jQuery(function($) {
	var winX   = 0;
	var mouseX = 0;
	var mouseY = 0;
	$(document).mousemove(function(event)
	{
		winX   = $(document).width();
		mouseX = event.pageX;
		mouseY = event.pageY;
	});
	function clicker(event)
	{
		var id = event.target.id;
		var oldID;
		var fID;
		var bID;
		var i;
		for(i=0;i<4;i++)
		{
			var iOID;
			var iBID;
			var iFID;
			switch(i)
			{
				case 0: iOID="GuideFront"; iBID="GuideLeft";  iFID="GuideRight"; break;
				case 1: iOID="GuideRight"; iBID="GuideFront"; iFID="GuideBack";  break;
				case 2: iOID="GuideBack";  iBID="GuideRight"; iFID="GuideLeft";  break;
				case 3: iOID="GuideLeft";  iBID="GuideBack";  iFID="GuideFront"; break;
			}
			if($("#div"+iOID).css("display") != "none")
			{
				oldID = iOID;
				bID   = iBID;
				fID   = iFID;
				break;
			}
		}
		if(id=="leftButton")
		{
			$("#div" + oldID).css("display","none");
			$("#div" + bID).css("display","inline");
		}
		else if(id == "rightButton")
		{
			$("#div" + oldID).css("display","none");
			$("#div" + fID).css("display","inline");
		}
	}
	$("#leftButton").click(clicker);
	$("#rightButton").click(clicker);

});

}

/*
     FILE ARCHIVED ON 14:28:02 Mar 04, 2020 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 04:56:19 May 15, 2026.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
*/
/*
playback timings (ms):
  capture_cache.get: 0.438
  load_resource: 173.603 (2)
  PetaboxLoader3.resolve: 93.742 (2)
  PetaboxLoader3.datanode: 56.838 (2)
*/