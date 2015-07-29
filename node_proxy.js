var net = require('net');
net.createServer(function (socket) {
	console.log('browser connected');

	var client = new net.Socket();
	client.connect(443, 'turn-euw2-ec2.browserstack.com', function() {
		console.log('connected to turn', client.remotePort, client.remoteAddress);
	});
	client.on('data', function(data) {
		// console.log('turn to browser ', data.toString());
		socket.write(data);
	});
	client.on('close', function() {
		console.log('turn connection closed');
	});
	client.on('error', function (error) {
  	console.log('turn error', error);
  });

  socket.on('close', function () {
  	console.log('browser close');
  });
  socket.on('error', function (error) {
  	console.log('browser error', error);
  });
  socket.on('data', function (data) {
  	setTimeout(function(){
  		// console.log('browser to turn ', data.toString());
	  	client.write(data);
	  },1000);
  });
}).listen(9001, '0.0.0.0');