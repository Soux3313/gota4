import React from 'react';
import './App.css';

class Playground extends React.Component {
  constructor(props) {
    super(props);
    

    //festlegen des Startzustandes
    this.state = {
      activePlayer: 1,
      phase: "select",
      startX: 0,
      startY: 0,
      endX: 0,
      endY: 0,
      shotX: 0,
      shotY: 0,
      legal: false,
      tableData: [
        [" ", "a", "b", "c", "d", "e", "f", "g", "h", "i","j"],
        [10, " ", " ", " ", "♛", " ", " ", "♛", " ", " ", " "],
        [9, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [8, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [7, "♛", " ", " ", " ", " ", " ", " ", " ", " ", "♛"],
        [6, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [5, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [4, "♕", " ", " ", " ", " ", " ", " ", " ", " ", "♕"],
        [3, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [2, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [1, " ", " ", " ", "♕", " ", " ", "♕", " ", " ", " "],
      ],
      fetchData: null,
    };
    
    // Bind the Game method to the component instance
    this.Game = this.Game.bind(this);
    this.checkIfFree = this.checkIfFree.bind(this);
  }

  componentDidMount() {
    // Make a GET request using fetch when the component mounts
    fetch('https://gruppe5.toni-barth.com') // Replace with your API endpoint
      .then((response) => response.json())
      .then((data) => {
        // Update the component's state with the fetched data
        this.setState({ fetchData: data });
      })
      .catch((error) => {
        console.error('Error fetching data:', error);
      });
  }

  //Testet ob die Figur welche an den Startkoordinaten steht 
  //einen freien weg zu den endkoordinaten hat und ob es auf einer 
  //Diagonalen,horizontalen oder vertikalen ebene steht
  checkIfFree(startX, startY, endX, endY) {
    
    const { tableData } = this.state;
    let legal = false;

    //wenn start=Ende
    if (startX === endX && (startY === endY)){
      return legal;
    }

    if (startX < 0 && startX >= tableData.length && endX < 0 && endX >= tableData[startX].length && startY < 0 && startY >= tableData.length && endY < 0 && endY >= tableData[startX].length) {
      return legal;
    }

    //wenn auf Einer Senkrechten Ebene
    if (startX === endX) {
      legal = true;

      //wenn nach oben bewegt
      if (startY < endY) {
        for (let n = startY + 1; n <= endY; n++) {
          if (tableData[startX][n] !== " ") {
            legal = false;
            break;
          }
        }

       //wenn nach unten bewegt
      } else {
        for (let n = startY - 1; n >= endY; n--) {
          if (tableData[endX][n] !== " ") {
            legal = false;
            break;
          }
        }
      }
      //wenn auf einer Horizontalen
    } else if (startY === endY) {
      legal = true;
      //wenn nach rechts bewegt
      if (startX < endX) {
        for (let n = startX + 1; n <= endX; n++) {
          if (tableData[n][startY] !== " ") {
            legal = false;
            break;
          }
        }
        //wenn nach links bewegt
      } else {
        for (let n = startX - 1; n >= endX; n--) {
          if (tableData[n][startY] !== " ") {
            legal = false;
            break;
          }
        }
      }
      //neben-diagonalen
    } else if (startX - startY === endX - endY) {
      legal = true;
      if (startX < endX) {
        let n = startY + 1;
        for (let m = startX + 1; m <= endX; m++) {
          if (tableData[m][n] !== " ") {
            legal = false;
            break;
          }
          n++;
        }
      } else {
        let n = startY - 1;
        for (let m = startX - 1; m >= endX; m--) {
          if (tableData[m][n] !== " ") {
            legal = false;
            break;
          }
          n--;
        }
      }
      //Haupt-diagonalen
    } else if (startX + startY === endX + endY) {
      legal = true;
      if (startX < endX) {
        let n = startY - 1;
        for (let m = startX + 1; m <= endX; m++) {
          if (tableData[m][n] !== " ") {
            legal = false;
            break;
          }
          n--;
        }
      } else {
        let n = startY + 1;
        for (let m = startX - 1; m >= endX; m--) {
          if (tableData[m][n] !== " ") {
            legal = false;
            break;
          }
          n++;
        }
      }
    }
    return legal;
  }

  /*Game(rowIndex, cellIndex) {
    let { activePlayer, phase, tableData } = this.state;
    let startX = this.state.startX;
    let startY = this.state.startY;
    let endX = this.state.endX;
    let endY = this.state.endY;
    let shotX = this.state.shotX;
    let shotY = this.state.shotY;
    let legal = this.state.legal;

    
    //wenn spieler 1 am zug ist und seine zu bewegende Königin wählt
    if (tableData[rowIndex][cellIndex] === "♕" && activePlayer === 1  && phase !=="shoot") {
      startX = rowIndex;
      startY = cellIndex;
      for (let i = startY - 1; i <= startY + 1; i++) {
        for (let j = startX - 1; j <= startX + 1; j++) {
          if (j > 0 && j < 11 && i > 0 && i < 11) {
            if (tableData[i][j] === " " && i !== 0) {
              phase = "move";
              break;
            }
          }
        }
      }
      //wenn spieler 2 am zug ist und seine zu bewegende Königin wählt
    } else if (tableData[rowIndex][cellIndex] === "♛" && activePlayer === 2 && phase !=="shoot") {
      startX = rowIndex;
      startY = cellIndex;
      for (let i = startY - 1; i <= startY + 1; i++) {
        for (let j = startX - 1; j <= startX + 1; j++) {
          if (tableData[i][j] === " " && i !== 0) {
            phase = "move";
            break;
          }
        }
      }
      //wenn spieler 1 am Zug ist und den Zielpunkt der zu bewegenden Königin wählt.
    } else if (phase === "move" && activePlayer === 1) {
      endX = rowIndex;
      endY = cellIndex;
      legal = this.checkIfFree(startX, startY, endX, endY);
      if (legal) {
        tableData[startX][startY] = " ";
        tableData[endX][endY] = "♕";
        phase = "shoot";
      }
      //wenn spieler 2 am Zug ist und den Zielpunkt der zu bewegenden Königin wählt.
    } else if (phase === "move" && activePlayer === 2) {
      endX = rowIndex;
      endY = cellIndex;
      legal = this.checkIfFree(startX, startY, endX, endY);
      if (legal) {
        tableData[startX][startY] = " ";
        tableData[endX][endY] = "♛";
        phase = "shoot";
      }
      //Wenn der Schuss gemacht werden soll (den treffenden Zielpunkt deaktivieren als Spielfeld)
    } else if (phase === "shoot") {
      shotX = rowIndex;
      shotY = cellIndex;
      legal = this.checkIfFree(endX, endY, shotX, shotY);
      if (legal) {
        tableData[rowIndex][cellIndex] = "🔥";
        const nextPlayer = activePlayer === 1 ? 2 : 1;
        this.setState({
          activePlayer: nextPlayer,
          phase: "select",
          startX: 0,
          startY: 0,
          endX: 0,
          endY: 0,
          shotX: 0,
          shotY: 0,
          legal: false,
          tableData: [...tableData], // Update tableData using the modified copy
        });

        //test ob ein Spieler gewonnen hat.
    if(this.playerWon(activePlayer)){
      alert("Spieler " + activePlayer + " hat gewonnen!")
    }
        return;
      }
    }


    //zurücksetzen der Stats und übernahme der Tabelle bzw des Spielfelds
    this.setState({
      activePlayer,
      phase,
      startX,
      startY,
      endX,
      endY,
      shotX,
      shotY,
      legal,
    });
  }
*/
Game(rowIndex, cellIndex) {
  let { activePlayer, phase, tableData } = this.state;
  let startX = this.state.startX;
  let startY = this.state.startY;
  let endX = this.state.endX;
  let endY = this.state.endY;
  let shotX = this.state.shotX;
  let shotY = this.state.shotY;
  let legal = this.state.legal;

  if (!tableData[rowIndex] || !tableData[rowIndex][cellIndex]) {
    return; // Frühzeitiger Ausstieg, wenn die Indizes ungültig sind
  }

  if (tableData[rowIndex][cellIndex] === "♕" && activePlayer === 1 && phase !== "shoot") {
    startX = rowIndex;
    startY = cellIndex;
    for (let i = startY - 1; i <= startY + 1; i++) {
      for (let j = startX - 1; j <= startX + 1; j++) {
        if (tableData[j] && tableData[j][i] === " " && i !== 0) {
          phase = "move";
          break;
        }
      }
    }
  } else if (tableData[rowIndex][cellIndex] === "♛" && activePlayer === 2 && phase !== "shoot") {
    startX = rowIndex;
    startY = cellIndex;
    for (let i = startY - 1; i <= startY + 1; i++) {
      for (let j = startX - 1; j <= startX + 1; j++) {
        if (tableData[j] && tableData[j][i] === " " && i !== 0) {
          phase = "move";
          break;
        }
      }
    }
  } else if (phase === "move" && activePlayer === 1) {
    endX = rowIndex;
    endY = cellIndex;
    legal = this.checkIfFree(startX, startY, endX, endY);
    if (legal) {
      tableData[startX][startY] = " ";
      tableData[endX][endY] = "♕";
      phase = "shoot";
    }
  } else if (phase === "move" && activePlayer === 2) {
    endX = rowIndex;
    endY = cellIndex;
    legal = this.checkIfFree(startX, startY, endX, endY);
    if (legal) {
      tableData[startX][startY] = " ";
      tableData[endX][endY] = "♛";
      phase = "shoot";
    }
  } else if (phase === "shoot") {
    shotX = rowIndex;
    shotY = cellIndex;
    legal = this.checkIfFree(endX, endY, shotX, shotY);
    if (legal) {
      tableData[rowIndex][cellIndex] = "🔥";
      const nextPlayer = activePlayer === 1 ? 2 : 1;
      this.setState({
        activePlayer: nextPlayer,
        phase: "select",
        startX: 0,
        startY: 0,
        endX: 0,
        endY: 0,
        shotX: 0,
        shotY: 0,
        legal: false,
        tableData: [...tableData],
      });

      if (this.playerWon(activePlayer)) {
        alert("Spieler " + activePlayer + " hat gewonnen!");
      }
      return;
    }
  }

  this.setState({
    activePlayer,
    phase,
    startX,
    startY,
    endX,
    endY,
    shotX,
    shotY,
    legal,
  });
}


  //test ob sich der nicht aktive Spieler noch bewegen kann, wenn nicht dann hat active Player gewonnen
  playerWon(activePlayer){
    var nextPlayer;
    if (activePlayer===1){
      nextPlayer= "♛";
    }else{
      nextPlayer="♕";
    }
      for (var n = 1; n < 11; n++) {
        for (var m = 1; m < 11; m++) {
          if (this.state.tableData[n][m] === nextPlayer) {
            for (var i = -1; i < 2; i++) {
              for (var j = -1; j < 2; j++) {
                if (j + m < 11 && j + m > 0 && i + n < 11 && i + n > 0) {
                  if (this.state.tableData[i + n][j + m] === " ") {
                    return false;
                  }
                }
              }
            }
          }
        }
      }
      return true;
    }
    
    //rücksetzen auf default
  handleReset() {
    this.setState({
      activePlayer: 1,
      phase: "select",
      startX: 0,
      startY: 0,
      endX: 0,
      endY: 0,
      shotX: 0,
      shotY: 0,
      legal: false,
      tableData: [
        [" ", "a", "b", "c", "d", "e", "f", "g", "h", "i","j"],
        [10, " ", " ", " ", "♛", " ", " ", "♛", " ", " ", " "],
        [9, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [8, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [7, "♛", " ", " ", " ", " ", " ", " ", " ", " ", "♛"],
        [6, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [5, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [4, "♕", " ", " ", " ", " ", " ", " ", " ", " ", "♕"],
        [3, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [2, " ", " ", " ", " ", " ", " ", " ", " ", " ", " "],
        [1, " ", " ", " ", "♕", " ", " ", "♕", " ", " ", " "],
      ]
    });
  }


  //aufbau des Spielfeldes in der Website
  render() {
    const bg = [
      ["reset", "a", "b", "c", "d", "e", "f", "g", "h", "i","j"],
      [10, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [9, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
      [8, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [7, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
      [6, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [5, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
      [4, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [3, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
      [2, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
    ];
    const renderCell = (rowIndex, cellIndex, value, cell) => {
      if (rowIndex === 0 || cellIndex === 0) {
        return <td key={cellIndex} className={cell === -1 ? "w" : cell === 0 ? "b" : "text"}>{value}</td>;
      } else {
        return <td key={cellIndex} className={cell === -1 ? "w" : cell === 0 ? "b" : "text"} onClick={() => this.Game(rowIndex, cellIndex)}>{value}</td>;
      }
    };

    const renderPlayer=() => {
      if (this.state.activePlayer === 1) {
        return <p>Player: White</p>
      } else {
        return <p>Player: Black</p>
      }
    };

    const renderPhase=()=> {
        return <p>Phase: {this.state.phase}</p>
      }

    //Ausgabe des HTML Codes - übergabe an React
    return (
      <div>
        <button onClick={() => this.handleReset()}>Reset</button>
        <div>{renderPlayer()}</div>  
        <div>{renderPhase()}</div>  
        <table>
        <tbody>
          {bg.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, cellIndex) => renderCell(rowIndex, cellIndex, this.state.tableData[rowIndex][cellIndex], cell))}
            </tr>
          ))}
        </tbody>
      </table>
      {this.state.fetchData && (
          <div>
            <h2>Fetched Data</h2>
            <pre>{JSON.stringify(this.state.fetchData, null, 2)}</pre>
          </div>
        )}
      </div>
    );
  }
}

export default Playground;
