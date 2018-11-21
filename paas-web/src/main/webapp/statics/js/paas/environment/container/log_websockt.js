window.docker = (function(docker) {
  docker.terminal = {
    startTerminalForContainer: function(host, container) {
      var term = new Terminal();
      term.open();

	var socket = new WebSocket('ws://' + location.host +'terminalService');
      var websocket = new WebSocket(wsUri);
      websocket.onopen = function(evt) { onOpen(evt) };
      websocket.onclose = function(evt) { onClose(evt) };
      websocket.onmessage = function(evt) { onMessage(evt) };
      websocket.onerror = function(evt) { onError(evt) };

      term.on('data', function(data) {
        websocket.send(data);
      });

      function onOpen(evt) {
        term.write("Session started");
      }

      function onClose(evt) {
        term.write("Session close");
      }

      function onMessage(evt) {
        term.write(evt.data);
      }

      function onError(evt) {
      }
    }
  };

  return docker;
})(window.docker || {});
