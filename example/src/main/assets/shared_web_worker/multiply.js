var first = document.querySelector('#number1');
var second = document.querySelector('#number2');

var result1 = document.querySelector('.result1');
console.log('multiply。js');

if (!!window.Worker) {
  var src = "onconnect = function(e) { onmessage = function(e) { var workerResult = 'Result: ' + (e.data[0] * e.data[1]); console.log('Posting message back to main script');postMessage(workerResult);}}"

  var myWorker = new Worker("data:text/javascript;charset=US-ASCII, " + src);

  first.onchange = function() {
    myWorker.postMessage([first.value, second.value]);
    console.log('Message posted to worker');
  }

  second.onchange = function() {
    myWorker.postMessage([first.value, second.value]);
    console.log('Message posted to worker');
  }

  myWorker.onmessage = function(e) {
    result1.textContent = e.data;
    console.log('Message received from worker');
    console.log(e.lastEventId);
  }
} else {
    console.log("Web Worker not supported.");
}
