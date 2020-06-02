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
 * Fetches the message from the servlet and displays it on the page
 */
function getMessage() {
  fetch('/data').then(response => response.json()).then((messages) => {
    console.log(messages);
    let div = document.createElement("div");
    div.className = 'servlet-message';
    div.textContent = constructMessage(messages);
    document.getElementById('htmlforms').appendChild(div);
  });
}

/**
 * Constructs a message from the given list of messages
 */
function constructMessage(messages) {
  return messages.join(' ');
}
