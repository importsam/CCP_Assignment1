
const rb = document.getElementById('recordBtn');
const localAudio = document.getElementById('localAudio')

let isRecording = false;
let mediaRecorder;

// Store audio chunks
let chunks = [];


// event listener for the recording button. Will indicate whether active or inactive.
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

// function to begin audio recording
function startRecording() {
	/* sources: 
	https://anwaarulislaam.medium.com/build-a-voice-recorder-app-using-only-javascript-and-html-212114a468ca
	https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Build_a_phone_with_peerjs/Connect_peers/Get_microphone_permission
	*/
	
	console.log("Started new recording")
	// access user microphone and take in audio.
	navigator.mediaDevices.getUserMedia({audio: true})
	.then((stream) => {
		window.localStream = stream;
		window.localAudio.srcObject = stream;
		window.localAudio.autoplay = true;
		
		
		mediaRecorder = new MediaRecorder(stream)
		
		// on stoppage, record the audio chunks and send to the API for transcription
		mediaRecorder.onstop = () => {
			const audioBlob = new Blob(chunks, { type: "audio/webm"});
			console.log("blob:", audioBlob);
			sendToTranscriptionAPI(audioBlob);
		}
		
		mediaRecorder.start();
		// Keeps pushing audio chunks during recording.
		mediaRecorder.ondataavailable = (e) => {
		  	chunks.push(e.data);
		};
	})
	.catch((err) => {
		console.error(`An error occurred: ${err}`)
	})
}
// stop the recording upon button press.
function stopRecording() {
	console.log("Stopped recording")
	mediaRecorder.stop();
	// stop all tracks
	if (window.localStream) {
	  	window.localStream.getTracks().forEach(track => track.stop());
	}
}

// function to send the audio blobs to the java /transcribe backend endpoint. 
function sendToTranscriptionAPI(audioBlob) {
	
  	const formData = new FormData();
  	formData.append('audio', audioBlob, 'recording.webm');

	// post request formation
	fetch('/transcribe', {
		method: 'POST',
		body: formData
	})
    .then(response => {
		if (!response.ok) {
			throw new Error(`Server error: ${response.status}`);
	  	}
	  	return response.text();
	})
    .then(transcription => {
      	console.log("Transcription:", transcription);
      	document.getElementById('transcriptionOutput').textContent = transcription;
    })
	.catch(err => console.error('Transcription call failed:', err));
}
