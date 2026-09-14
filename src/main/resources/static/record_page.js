
const rb = document.getElementById('recordBtn');
const localAudio = document.getElementById('localAudio')

let isRecording = false;
let mediaRecorder;

recordBtn.addEventListener('click', function() {
  if (!isRecording) {
	startRecording();
    recordBtn.textContent = 'Stop Recording';
  } else {
    recordBtn.textContent = 'Start Recording';
	stopRecording();
  }
  isRecording = !isRecording;
});

function startRecording() {
	console.log("Started new recording")
	navigator.mediaDevices.getUserMedia({audio: true})
	.then((stream) => {
		window.localStream = stream;
		window.localAudio.srcObject = stream;
		window.localAudio.autoplay = true;
		
		mediaRecorder = new MediaRecorder(stream)
		mediaRecorder.start();
	})
	.catch((err) => {
		console.error(`An error occurred: ${err}`)
	})
}

function stopRecording() {
	console.log("Stopped recording")
	mediaRecorder.stop();
}

