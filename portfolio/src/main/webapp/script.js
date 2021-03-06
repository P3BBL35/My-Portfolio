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
 * Insert all messages from the given list into a new div on the page.
 */
function getMessages(messages) {
    console.log(messages);

    let commentsDiv = document.getElementById('display-comments');

    let sortNum = messages[messages.length - 1];
    let semicolonIndex = sortNum.indexOf(";");
    let sortOrder = sortNum.substring(semicolonIndex + 1, sortNum.length);
    addParameter('numComments', sortNum.substring(0, semicolonIndex));
    addParameter('commentSort', sortOrder);

    commentsDiv.innerHTML = '';

    // Order the comments appropriately. Default value is by newest.
    for (index = 0; index < messages.length - 1; index++) {
      addComment(commentsDiv, messages, index);
    }
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
    document.getElementById('display-comments').innerHTML = '';
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
 * Reorder the messages in the order selected by the user.
 */
function getOrder() {
  let select = document.getElementById('commentSort');
  addParameter('commentSort', select.options[select.selectedIndex].value);
}

/**
 * Checks whether the user is logged in or not. If the user is logged in, then
 * display the comments section. Otherwise, the comments section will remain
 * hidden, and the user is directed to a login page.
 */
function isLoggedIn() {

  fetch('/login').then(response => response.json()).then((loginInfo) => {
    let loginStatus = loginInfo[0];
    console.log(loginInfo);
    console.log('User login status: ' + loginStatus);

    let comments = document.getElementById("htmlforms");
    let comSection = document.getElementById("comments-section");
    let login = document.getElementById("login");
    let logout = document.getElementById("logout");
    let welcome = document.getElementById("welcome-message");
    let nickname = document.getElementById("set-nickname");

    if (loginStatus == 'false') {
      let loginURL = loginInfo[1];

      welcome.innerHTML = "<h2>You are not logged in</h2>";
      nickname.style.display = 'none';
      comments.style.display = 'none';
      comSection.style.display = 'none';
      logout.style.display = 'none';
      login.style.display = 'inline';
      login.innerHTML = "<p><a href=\"" + loginURL + "\">Login Here</a></p>";
    } else {
      let logoutURL = loginInfo[1];
      let displayName = loginInfo[2];
      
      let h2 = document.createElement("h2");
      h2.textContent = "Welcome, " + displayName;
      welcome.appendChild(h2);
      
      nickname.style.display = 'inline';
      comments.style.display = 'initial';
      comSection.style.display = 'initial';
      login.style.display = 'none';
      logout.style.display = 'inline';
      logout.innerHTML = "<p><a href=\"" + logoutURL + "\">Logout Here</a></p>";
    }
  });
}

google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);


/**
 * Callback that creates and populates a data table, instantiates
 * a pie chart, and draws it.
 */
function drawChart() {
  fetch('/charts').then(response => response.json()).then((data) => {
    console.log(data);

    let dataTable = new google.visualization.DataTable();
    dataTable.addColumn('string', 'Gender');
    dataTable.addColumn('number', 'Very Favorable');
    dataTable.addColumn('number', 'Somewhat Favorable');
    dataTable.addColumn('number', 'Somewhat Unfavorable');
    dataTable.addColumn('number', 'Very Unfavorable');
    dataTable.addColumn('number', 'Heard Of; No Opinion');
    dataTable.addColumn('number', 'Never Heard of');
   
    Object.keys(data).forEach((gender) => {
      let gender_stats = data[gender];
      dataTable.addRow([
          gender,
          gender_stats[0],
          gender_stats[1],
          gender_stats[2],
          gender_stats[3],
          gender_stats[4],
          gender_stats[5]
      ]);
    });
    let options = {'title': 'Favorability of Anime Movies Based on Gender',
                   'width': '700',
                   'height': '500',
                   'isStacked': 'percent'};
    
    let chart = new google.visualization.ColumnChart(document.getElementById("charts-id"));
    chart.draw(dataTable, options);
  });
}

function loadWindow(imageLink) {
  let modal = document.getElementById("modal");
  modal.style.display = 'block';
   
  let content = document.getElementById("modal-content");
  let image = document.createElement('img');
  image.id = "display-image";
  image.src = "images/" + imageLink;
  content.appendChild(image);
}

function galleryOnLoad() {
  let modal = document.getElementById("modal");
  let close = document.getElementById("close");
  let content = document.getElementById("modal-content");

  close.onclick = function() {
    modal.style.display = 'none';
    content.removeChild(content.lastChild);
  }

  window.onclick = function(event) {
    if (event.target == modal) {
      modal.style.display = 'none';
      content.removeChild(content.lastChild);
    }
  }
}
