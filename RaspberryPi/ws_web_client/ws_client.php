<?php
$x = isset($_GET['x']) ? $_GET['x']: 400;
$y = isset($_GET['y']) ? $_GET['y']: 400;
?>
<html>
  <head>
    <title>WebSockets with Python & Tornado</title>
    <meta charset="utf-8" />
    <style type="text/css">
		body {
			min-width: 600px;
			text-align: center;
		}
		#gcode {
			display: inline-block;
		}
		#log {
			width:400px;
			height:<? print $y; ?>px;
			overflow:scroll;
			right:20px;
			margin:auto;
			text-align:left;
			background-color:#eeeeee; 
		}
		#msg{
			width:100%;
		}
		#mycanvas{
			border: 1px solid gray;
			width: <? print $x; ?>px;
			height: <? print $y; ?>px;
			margin: 5;
		}
    </style>
    <script src="jquery.js"></script>
    <script src="jcanvas.min.js"></script>
    <script>
      $(function(){
        var ws;
        var logger = function(msg){
          var now = new Date();
          var sec = now.getSeconds();
          var min = now.getMinutes();
          var hr = now.getHours();
          $("#log").html($("#log").html() + "<br/>" + hr + ":" + min + ":" + sec + " ___ " +  msg);
          $('#log').scrollTop($('#log')[0].scrollHeight);
        }

        var sender = function() {
          var msg = $("#msg").val();
          if (msg.length > 0)
            ws.send(msg);
          $("#msg").val(msg);
        }

        ws = new WebSocket("ws://127.0.0.1:22300/ws");
        ws.onmessage = function(evt) {
          logger(evt.data);
        };
        ws.onclose = function(evt) { 
          $("#log").text("Connection was closed..."); 
          $("#thebutton #msg").prop('disabled', true);
        };
        ws.onopen = function(evt) { $("#log").text("Opening socket..."); };

        $("#msg").keypress(function(event) {
          if (event.which == 13) {
             sender();
           }
        });

        $("#thebutton").click(function(){
          sender();
        });
        
        $("canvas").drawRect({
			layer: true,
			fillStyle: "#eee",
			x: 0, y: 0,
			width: <? print $x; ?>,
			height: <? print $y; ?>,
			fromCenter: false,
			click: function(layer) {
		        var dx, dy, dist;
				dx = Math.round(layer.eventX - layer.x);
				dy = Math.round(layer.eventY - layer.y);
				l1=Math.round(Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2)));
				l2=Math.round(Math.sqrt(Math.pow(<? print $x; ?>-dx,2)+Math.pow(dy,2)));
				console.info(dx +', '+ dy);
				$("canvas").removeLayer("x");
				$("canvas").drawLine({
					layer: true,
					name: "x",
					strokeStyle: "#00F",
					strokeWidth: 2,
					x1: dx, y1: 0,
					x2: dx, y2: dy,
				});
				$("canvas").removeLayer("y");
				$("canvas").drawLine({
					layer: true,
					name: "y",
					strokeStyle: "#00F",
					strokeWidth: 2,
					x1: 0, y1: dy,
					x2: dx, y2: dy,
				});
				$("canvas").removeLayer("l1");
				$("canvas").drawLine({
					layer: true,
					name: "l1",
					strokeStyle: "#F00",
					strokeWidth: 2,
					x1: 0, y1: 0,
					x2: dx, y2: dy,
				});
				$("canvas").removeLayer("l2");
				$("canvas").drawLine({
					layer: true,
					name: "l2",
					strokeStyle: "#F00",
					strokeWidth: 2,
					x1: <? print $x; ?>, y1: 0,
					x2: dx, y2: dy,
				});
				angle1 = Math.round(180*Math.asin(dy/l1)/3.14159);
				$("canvas").drawText({
					strokeStyle: "#000",
					fillStyle: "#000",
					strokeWidth: 1,
					x: dx/2, y: dy/2,
					font: "12pt Arial",
					text: l1,
					fromCenter: true,
					rotate: angle1,
				});
				angle2 = Math.round(-180*Math.asin(dy/l2)/3.14159);
				$("canvas").drawText({
					strokeStyle: "#000",
					fillStyle: "#000",
					strokeWidth: 1,
					x: dx+(<? print $x; ?>-dx)/2, y: dy/2,
					font: "12pt Arial",
					text: l2,
					fromCenter: true,
					rotate: angle2,
				});
				$("canvas").drawText({
					strokeStyle: "#000",
					fillStyle: "#000",
					strokeWidth: 1,
					x: dx/2, y: dy,
					font: "12pt Arial",
					text: Math.round(dx),
				});
				$("canvas").drawText({
					strokeStyle: "#000",
					fillStyle: "#000",
					strokeWidth: 1,
					x: dx, y: dy/2,
					font: "12pt Arial",
					text: Math.round(dy),
				});
				ws.send("G0X"+dx+"Y"+dy+"Z0");
			}
		});
      });
    </script>
  </head>

  <body>
    <h1 id='title'>Illustrabot Web Client</h1>
    <div style='display: bloc;'>
		<div id="gcode">
			<div id="log">logs</div>
			<div id="manual-gcode">
			  <input type="text" id="msg" value="G0X0Y0Z0" style="background:#fff;width:200px">
			  <input type="button" id="thebutton" value="Send" />
			</div>
		</div>
		<canvas id='mycanvas' width=<? print $x; ?> height=<? print $y; ?>>
			This text is displayed if your browser does not support HTML5 Canvas.
		</canvas>
	</div>
    <a href="http://lowpowerlab.com/blog/2013/01/17/raspberrypi-websockets-with-python-tornado/">Inspired from www.LowPowerLab.com</a>
  </body>
</html>
