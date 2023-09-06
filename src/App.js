import React from 'react';
import Playground from './Spielfeld';
import './App.css';


class App extends React.Component {
  render() {
    return (
      <body>
        <h1>Game of the Amazons</h1>
        <div class="container">
          <div >
            {/* Spielfeld */}
            <Playground class="playground"></Playground>
          </div>
          <div class="help">
            <details class="help">  {/* Hilfe Objekte zur Erklärung des Spiels */}
              <summary ><h2>Hilfe</h2></summary>
              <details>
                <summary><h3>Spielanleitung</h3></summary>
                <ol>
                  <li>Eigene Dame wählen.</li>
                  <li>Wählen wohin die Dame gesetzt werden soll(Horizontal,Vertikal,Diagonal).</li>
                  <li>Wählen welches Feld für alle Spieler gesperrt werden soll(Horizontal,Vertikal,Diagonal).</li>
                </ol>
              </details>
              <details>
                <summary><h3>Wie man gewinnt</h3></summary>
                <div><p> Blocke die gegnerischen Spielfiguren, sodass sie kein Feld mehr haben sich zu bewegen bevor es mit deinen Figuren passiert</p></div>
              </details>
              <details>
                <summary><h3>Wie bewegen sich die Spielfiguren?</h3></summary>
                <p>
                  Vertikal, Horizontal und Diagonal in alle Richtungen, jedoch kann man nicht durch blockierte und besetzte Felder hindurchgehen.
                </p>
              </details>
              <details>
                <summary><h3>Bugs?</h3></summary>
                <p>
                  Es gibt keine Bugs, sondern nur neue Spielmechaniken von uns den tollen Developers ;D
                </p>
              </details>
            </details>
            </div>
          
          {/* Footer mit Text für das Quasi Impressum */}
        <footer class="footer">
          <p >
            Diese Website wurde im Auftrag von der Hochschule Anhalt unter der Aufsicht von Toni Barth entwickelt.<br />
            Autoren: Sophie Schmeiduch, Paul Hanemann, Franz Georgi, Ricardo Hoppe
          </p>
        </footer>
        </div>
      </body>
      
    );
  }
}

export default App;