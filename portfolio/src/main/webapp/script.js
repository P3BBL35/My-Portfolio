// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['No act of kindness, no matter how small, is ever wasted.', 
           'We mature with the damage, not the years.', 
	   'We pretend that we don\'t care, but we do care.', 
	   'Never make permanent decisions on temporary feelings.',
           'A person who never made a mistake never tried anything new.',
           'When nothing goes right...go left.',
           'My wallet is like an onion: when I open it, I cry.',
           'Education is learning what you didn\'t even know you didn\'t know.',
           'Don\'t cry because it\'s over. Smile because it happened.'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Updates the sort order and number of comments to display in the server,
 * and fetches them according to the new parameters.
 */
function updateMessage() {
  let headers = new Headers();
  headers.append('numComments', getParameter('numComments'));
  headers.append('commentSort', getParameter('commentSort'));
  headers.append('change', true);

  fetch('/data', {headers: headers}).then(response => response.json()).then((messages) => 
      getMessages(messages));
}

/**
 * Fetches the message from the servlet and displays it on the page.
 */
function initMessages() {
  fetch('/data').then(response => response.json()).then((messages) => getMessages(messages));
}

/**
 * Insert all messages from the given list into a new div on the page
 */
function getMessages(messages) {
    console.log(messages);

    let section = document.getElementById('display-comments');
    let commentsDiv = document.createElement('div');

    let sortNum = messages[messages.length - 1];
    let colonIndex = sortNum.indexOf(";");
    let sortOrder = sortNum.substring(colonIndex + 1, sortNum.length);
    addParameter('numComments', sortNum.substring(0, colonIndex));
    addParameter('commentSort', sortOrder);

    section.innerHTML = '';

    // Order the comments appropriately. Default value is by newest.
    for (index = 0; index < messages.length - 1; index++) {
      addComment(commentsDiv, messages, index);
    }
    section.appendChild(commentsDiv);

}

/**
 * Adds a comment/message at the given index in the messages list into the comments div.
 */
function addComment(commentsDiv, messages, index) {
  let comment = document.createElement('p');
  comment.textContent = messages[index];

  commentsDiv.appendChild(comment);
}

function deleteMessages() {
  fetch('/delete-data', {method: 'POST'}).then(() => {
    document.getElementById('display-comments').innerHTML = 
        '<p>Nothing to see here!</p><br/>';
  });
}

/**
 * Gets a parameter with the given name from the query URL.
 */
function getParameter(name) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(name);
}

/**
 * Adds a parameter with the given name and value to the query URL.
 */
function addParameter(name, value) {
  const urlParams = new URLSearchParams(window.location.search);
  urlParams.set(name, value);
  let newQuery = window.location.pathname + '?' + urlParams.toString();
  history.pushState(null, '', newQuery);
}

/**
 * Reorder the messages in the order selected by the user
 */
function getOrder() {
  let select = document.getElementById('commentSort');
  addParameter('commentSort', select.options[select.selectedIndex].value);
}
