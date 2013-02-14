<html>
  <head>
    <title>WebSockets with Python & Tornado</title>
    <meta charset="utf-8" />
    <style type="text/css">
      body {
        text-align: center;
        min-width: 500px;
      }
    </style>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
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

        ws = new WebSocket("ws://mypi:22300/ws");
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
      });
    </script>
  </head>

  <body>
    <h1>WebSockets with Python & Tornado</h1>
    <div id="log" style="overflow:scroll;width:500px; height:200px;background-color:#ffeeaa; margin:auto; text-align:left">Messages go here</div>

    <div style="margin:10px">
      <input type="text" id="msg" style="background:#fff;width:200px"/>
      <input type="button" id="thebutton" value="Send" />
    </div>

    <a href="http://lowpowerlab.com/blog/2013/01/17/raspberrypi-websockets-with-python-tornado/">www.LowPowerLab.com</a>
  </body>
</html>
