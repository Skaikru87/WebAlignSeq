'use strict';

var singleUploadForm = document.querySelector('#singleUploadForm');
var singleFileUploadInput = document.querySelector('#singleFileUploadInput');
var singleFileUploadError = document.querySelector('#singleFileUploadError');
var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');

var singleAdditionalUploadForm = document.querySelector('#singleAdditionalUploadForm');
var singleAdditionalFileUploadInput = document.querySelector('#singleAdditionalFileUploadInput');
var singleAdditionalFileUploadError = document.querySelector('#singleAdditionalFileUploadError');
var singleAdditionalFileUploadSuccess = document.querySelector('#singleAdditionalFileUploadSuccess');

function uploadSingleFile(file) {
    var formData = new FormData();
    formData.append("file", file);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadFile");

    xhr.onload = function () {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);

        if (xhr.status === 200) {
            singleFileUploadError.style.display = "none";
            singleFileUploadSuccess.innerHTML = "<p>File Uploaded Successfully.</p><p>Click here to download your excel file: <a href='" + response.fileDownloadUri + "' target='_blank'>" + response.fileDownloadUri + "</a></p>";
            singleFileUploadSuccess.style.display = "block";
        } else {
            singleFileUploadSuccess.style.display = "none";
            singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }
    xhr.send(formData);
}

function waitDisplay() { // Submit button clicked
    singleFileUploadSuccess.innerHTML = "<p>please wait...your file is processing... it may take few minutes</p>"
    return true;
}
function validateFileType(input){
    var fileName = input.value,
        idxDot = fileName.lastIndexOf(".") + 1,
        extFile = fileName.substr(idxDot, fileName.length).toLowerCase();
    if (["xlsx", "css"].includes(extFile)){

        singleFileUploadSuccess.innerHTML = "<p>correct file... please click Submit to launch the program</p>"
    } else {
        alert("Only excel files are allowed!");
        input.value = ""
    }
}

function uploadSingleAdditionalFile(additionalFile) {
    var formAdditionalData = new FormData();
    formAdditionalData.append("additionalFile", additionalFile);

    var xhrAdd = new XMLHttpRequest();
    xhrAdd.open("POST", "/uploadAdditionalFile");
    xhrAdd.onload = function () {
        console.log(xhrAdd.responseText);
        var response = JSON.parse(xhrAdd.responseText);
        if (xhrAdd.status == 200) {
            singleAdditionalFileUploadError.style.display = "none";
            singleAdditionalFileUploadSuccess.innerHTML = "<p>File Uploaded Successfully.</p>";
            singleAdditionalFileUploadSuccess.style.display = "block";
        } else {
            singleAdditionalFileUploadSuccess.style.display = "none";
            singleAdditionalFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }
    xhrAdd.send(formAdditionalData);
}



singleAdditionalUploadForm.addEventListener('submit', function (event) {
    var files = singleAdditionalFileUploadInput.files;
    if (files.length === 0) {
        singleAdditionalFileUploadError.innerHTML = "Please select a file";
        singleAdditionalFileUploadError.style.display = "block";
    }
    uploadSingleAdditionalFile(files[0]);
    event.preventDefault();
}, true);

singleUploadForm.addEventListener('submit', function (event) {
    var files = singleFileUploadInput.files;
    if (files.length === 0) {
        singleFileUploadError.innerHTML = "Please select a file";
        singleFileUploadError.style.display = "block";
    }
    uploadSingleFile(files[0]);
    event.preventDefault();
}, true);

