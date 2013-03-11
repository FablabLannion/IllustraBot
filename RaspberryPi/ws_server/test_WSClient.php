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
			height:400px;
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
			width: 400px;
			height: 400px;
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
          //$("#log").animate({ scrollTop: $('#log')[0].scrollHeight}, 100);
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
			width: 400,
			height: 400,
			fromCenter: false,
			click: function(layer) {
		        var dx, dy, dist;
				dx = layer.eventX - layer.x;
				dy = layer.eventY - layer.y;
				console.info(dx +', '+ dy);
			}
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 0, y: 0,
			radius: 10,
			click: function(layer) { ws.send("G0X0Y0Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 200, y: 0,
			radius: 10,
			click: function(layer) { ws.send("G0X200Y0Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 400, y: 0,
			radius: 10,
			click: function(layer) { ws.send("G0X400Y0Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 0, y: 200,
			radius: 10,
			click: function(layer) { ws.send("G0X0Y200Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 200, y: 200,
			radius: 10,
			click: function(layer) { ws.send("G0X200Y200Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 400, y: 200,
			radius: 10,
			click: function(layer) { ws.send("G0X400Y200Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 0, y: 400,
			radius: 10,
			click: function(layer) { ws.send("G0X0Y400Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 200, y: 400,
			radius: 10,
			click: function(layer) { ws.send("G0X200Y400Z0"); }
		});
		$("canvas").drawArc({
			layer: true,
			fillStyle: "gray",
			x: 400, y: 400,
			radius: 10,
			click: function(layer) { ws.send("G0X400Y400Z0"); }
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
		<canvas id='mycanvas' width=400 height=400>
			This text is displayed if your browser does not support HTML5 Canvas.
		</canvas>
	</div>
    <a href="http://lowpowerlab.com/blog/2013/01/17/raspberrypi-websockets-with-python-tornado/">Inspired from www.LowPowerLab.com</a>
  </body>
</html>
