from termcolor import colored
import time
from multiprocessing import Pool, cpu_count
from typing import List, Tuple


class GameField:
    def __init__(self):
        b0, b1, b2, b3 = (3, 0), (0, 3), (0, 6), (3, 9)
        w0, w1, w2, w3 = (6, 0), (9, 3), (9, 6), (6, 9)
        self.w = [w0, w1, w2, w3]
        self.b = [b0, b1, b2, b3]
        self.arrows = []

    class IllegalMove(Exception):
        pass

    def to_board_matrix(self) -> List[List[str]]:
        board = []
        for x in range(0, 10):
            board.append([])
            for y in range(0, 10):
                board[x].append("e")
                if (x, y) in self.w:
                    board[x][y] = "w" + str(self.w.index((x, y)))
                elif (x, y) in self.b:
                    board[x][y] = "b" + str(self.b.index((x, y)))
                elif (x, y) in self.arrows:
                    board[x][y] = "X"
        return board

    def find_piece(self, piece: str) -> Tuple[int, int]:
        cords = None
        if piece.startswith("w"):
            cords = self.w[int(piece[1:])]
        if piece.startswith("b"):
            cords = self.b[int(piece[1:])]
        return cords

    def fire_arrow(self, piece: str, x: int, y: int) -> None:
        cords = self.find_piece(piece)
        if not self.is_legal(cords[0], cords[1], x, y):
            raise GameField.IllegalMove
        self.arrows.append((x, y))

    def move_piece(self, piece: str, x: int, y: int, ignore_legality: bool = False) -> None:
        cords = self.find_piece(piece)
        if not self.is_legal(cords[0], cords[1], x, y) and not ignore_legality:
            raise GameField.IllegalMove
        if piece.startswith("w"):
            self.w[int(piece[1:])] = (x, y)
        if piece.startswith("b"):
            self.b[int(piece[1:])] = (x, y)

    def is_legal(self, source_x: int, source_y: int, dest_x: int, dest_y: int) -> bool:
        dest = (dest_x, dest_y)
        direction_y = 0
        direction_x = 0
        board = self.to_board_matrix()
        if source_x == dest_x and source_y == dest_y:
            return False
        if source_x != dest_x and source_y == dest_y:
            if source_x > dest_x:
                direction_x = -1
            elif source_x < dest_x:
                direction_x = 1
        elif source_y != dest_y and source_x == dest_x:
            if source_y > dest_y:
                direction_y = -1
            elif source_y < dest_y:
                direction_y = 1
        elif abs(source_x - dest_x) == abs(source_y - dest_y) != 0:
            if source_x > dest_x:
                direction_x = -1
            elif source_x < dest_x:
                direction_x = 1
            if source_y > dest_y:
                direction_y = -1
            elif source_y < dest_y:
                direction_y = 1
        cur_x = source_x
        cur_y = source_y
        while 0 <= cur_x < 10 and 0 <= cur_y < 10:
            cur_x += direction_x
            cur_y += direction_y
            if board[cur_x][cur_y] != "e":
                return False
            if (cur_x, cur_y) == dest:
                return True
        return False

    def simple__str__(self) -> str:
        result = "  0  1  2  3  4  5  6  7  8  9\n"
        board = self.to_board_matrix()
        for x in range(0, 10):
            line = str(y)
            for y in range(0, 10):
                line += " " + board[x][y] + " "
            result += str(line) + "\n"
        return result

    def __str__(self) -> str:
        result = ""
        board = self.to_board_matrix()
        for x in range(0, 10):
            line = ""
            for y in range(0, 10):
                char = board[x][y]
                if char == "e":
                    char = str(x) + str(y)
                    if (x + y) % 2 == 0:
                        char = colored(char, color='white')
                    else:
                        char = colored(char, color='blue')
                if char.startswith("w"):
                    char = colored(char, color='grey', on_color="on_white")
                if char.startswith("b"):
                    char = colored(char, color='grey', on_color="on_blue")
                if char == "X":
                    char = colored(char + " ", color="red")
                line += char + " "
            result += str(line) + "\n"
        return result[:-1]


class Player:
    def __init__(self, name):
        self.name = name

    def play(self, gamefield, color):
        raise NotImplemented


class HumanPlayer(Player):
    def __init__(self, name):
        super().__init__(name)
        self.name = name

    def play(self, gamefield, color):
        i1 = input("Wer soll bewegt werden? (0/1/2/3)")
        i2 = input("Wohin soll sie bewegt werden?  (xy)")
        i3 = input("Wohin soll gefeuert werden? (xy)")
        piece = color + i1
        x1, y1 = int(i2[0]), int(i2[1])
        x2, y2 = int(i3[0]), int(i3[1])
        return piece, x1, y1, x2, y2


class Game:
    def __init__(self, player1: Player, player2: Player, cli_output: bool = True):
        assert player1 is not player2
        self.gf = GameField()
        self.cli_output = cli_output
        self.p1 = player1
        self.p2 = player2
        self.p1_turn_times = []
        self.p2_turn_times = []

    def cond_print(self, *args) -> None:
        if self.cli_output:
            print(*args)

    def check_any_movement_possible(self, x: int, y: int) -> bool:
        directions = ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, 1), (-1, -1), (1, -1))
        for direction in directions:
            cur_x = x + direction[0]
            cur_y = y + direction[1]
            try:
                assert cur_x >= 0
                assert cur_y >= 0
                field = self.gf.to_board_matrix()[cur_x][cur_y]
            except (IndexError, AssertionError):
                continue
            if field == "e":
                return True
        return False

    def can_player_move(self, color: str) -> bool:
        queens = []
        if color == "b":
            queens = self.gf.b
        elif color == "w":
            queens = self.gf.w
        for queen in queens:
            if self.check_any_movement_possible(queen[0], queen[1]):
                return True
        return False

    def start_gameloop(self) -> Tuple[Player, int, List[float], List[float], float]:
        cur_turn = 1
        overall_start_time = time.time()
        while True:
            self.cond_print("--- Start von Runde", cur_turn)
            for color, player in (("w", self.p1), ("b", self.p2)):
                self.cond_print("--", player.name, color, "ist am Zug")
                self.cond_print(self.gf)
                if not self.can_player_move(color):
                    self.cond_print(player.name, "kann sich nicht mehr bewegen.")
                    overall_time = time.time() - overall_start_time
                    if player == self.p1:
                        return self.p2, cur_turn, self.p1_turn_times, self.p2_turn_times, overall_time
                    elif player == self.p2:
                        return self.p1, cur_turn, self.p1_turn_times, self.p2_turn_times, overall_time
                start_time = time.time()
                while True:
                    try:
                        piece, x1, y1, x2, y2, *score = player.play(self.gf, color)
                        old_pos = self.gf.find_piece(piece)
                        self.gf.move_piece(piece, x1, y1)
                        self.gf.fire_arrow(piece, x2, y2)
                        self.cond_print(
                            "{} wurde auf ({}|{}) bewegt und hat auf ({}|{}) geschossen.".format(piece, str(x1),
                                                                                                 str(y1), str(x2),
                                                                                                 str(y2)))
                        if score:
                            self.cond_print("Der Zug hat einen Score von", score[0])
                        if player == self.p1:
                            self.p1_turn_times.append(time.time() - start_time)
                        elif player == self.p2:
                            self.p2_turn_times.append(time.time() - start_time)
                        self.cond_print("Dieser Zug hat {0:.2f} Sekunden gedauert.".format(time.time() - start_time))
                        break
                    except GameField.IllegalMove:
                        self.gf.move_piece(piece, old_pos[0], old_pos[1], True)
                        self.cond_print(colored("-------Das ist kein gültiger Zug, versuche es erneut.", color="red"))
            cur_turn += 1


class Tournament:
    def __init__(self, games_per_matchup, players):
        self.games_per_matchup = games_per_matchup
        self.players = players
        self.wins = dict()
        self.wins_as_white = dict()
        self.wins_as_black = dict()
        self.calculation_times = dict()
        self.round_length_win = dict()
        self.round_length_loss = dict()
        for player in self.players:
            self.wins[player] = 0
            self.wins_as_white[player] = 0
            self.wins_as_black[player] = 0
            self.calculation_times[player] = []
            self.round_length_win[player] = []
            self.round_length_loss[player] = []

    def run(self):
        num_of_games = (len(self.players) * (len(self.players) - 1) * self.games_per_matchup)
        print("There will be {} matches".format(num_of_games))
        percentage = 100 / num_of_games
        progress = 0
        for white in self.players:
            for black in self.players:
                if white is black:
                    continue
                print("-- {} (white) vs {} (black)".format(white.name, black.name))
                for i in range(self.games_per_matchup):
                    game = Game(white, black, False)
                    winner, rounds, times1, times2, overall_time = game.start_gameloop()
                    progress += percentage
                    print("({:05.2f}%) ROUND {}: {} won in {} rounds ({:.2f}s)".format(
                        progress, i + 1, winner.name, rounds, overall_time))
                    loser = None
                    if winner is white:
                        self.wins_as_white[winner] += 1
                        loser = black
                    elif winner is black:
                        self.wins_as_black[winner] += 1
                        loser = white
                    self.wins[winner] += 1
                    self.calculation_times[white].extend(times1)
                    self.calculation_times[black].extend(times2)
                    self.round_length_win[winner].append(rounds)
                    self.round_length_loss[loser].append(rounds)
        self.print_report()

    def print_report(self):
        print("---- REPORT ----")
        num_of_games = (len(self.players) - 1) * self.games_per_matchup
        for player in self.players:
            print("-- " + player.name)
            print("Win Rate                                 : {:.2%} ({}/{})".format(
                self.wins[player] / (num_of_games * 2),
                self.wins[player], num_of_games * 2))
            print("Win Rate as white                        : {:.2%} ({}/{})".format(
                self.wins_as_white[player] / num_of_games,
                self.wins_as_white[player], num_of_games))
            print("Win Rate as black                        : {:.2%} ({}/{})".format(
                self.wins_as_black[player] / num_of_games,
                self.wins_as_black[player], num_of_games))
            print("Calculation time                         : min {:.2f}s; max {:.2f}s; avg {:.2f}s".format(
                min(self.calculation_times[player]),
                max(self.calculation_times[player]),
                (sum(self.calculation_times[player]) / len(self.calculation_times[player]))))
            round_length = self.round_length_win[player]
            if not round_length:
                round_length = [-1]
            print("Round length when won (lower is better)  : min {:.2f}; max {:.2f}; avg {:.2f}".format(
                min(round_length),
                max(round_length),
                (sum(round_length) / len(round_length))))
            round_length = self.round_length_loss[player]
            if not round_length:
                round_length = [-1]
            print("Round length when lost (higher is better): min {:.2f}; max {:.2f}; avg {:.2f}".format(
                min(round_length),
                max(round_length),
                (sum(round_length) / len(round_length))))


class TournamentMP(Tournament):
    def __init__(self, threads, games_per_matchup, players):
        self.threads = threads
        self.games_per_matchup = games_per_matchup
        self.players = players
        self.wins = dict()
        self.wins_as_white = dict()
        self.wins_as_black = dict()
        self.calculation_times = dict()
        self.round_length_win = dict()
        self.round_length_loss = dict()
        for player in self.players:
            self.wins[player.name] = 0
            self.wins_as_white[player.name] = 0
            self.wins_as_black[player.name] = 0
            self.calculation_times[player.name] = []
            self.round_length_win[player.name] = []
            self.round_length_loss[player.name] = []

    def run(self):
        num_of_games = (len(self.players) * (len(self.players) - 1) * self.games_per_matchup)
        print("There will be {} matches".format(num_of_games))
        args = []
        for white in self.players:
            for black in self.players:
                if white is black:
                    continue
                for i in range(self.games_per_matchup):
                    args.append((white, black))
        pool = Pool(processes=self.threads)
        results = pool.starmap(TournamentMP.run_match, args)
        pool.close()
        for result in results:
            white = result[0]
            black = result[1]
            winner, rounds, times1, times2, overall_time = result[2]
            loser = None
            if winner is white:
                self.wins_as_white[winner.name] += 1
                loser = black
            elif winner is black:
                self.wins_as_black[winner.name] += 1
                loser = white
            self.wins[winner.name] += 1
            self.calculation_times[white.name].extend(times1)
            self.calculation_times[black.name].extend(times2)
            self.round_length_win[winner.name].append(rounds)
            self.round_length_loss[loser.name].append(rounds)
        self.print_report()

    @staticmethod
    def run_match(white, black):
        game = Game(white, black, False)
        return white, black, game.start_gameloop()

    def print_report(self):
        print("---- REPORT ----")
        num_of_games = (len(self.players) - 1) * self.games_per_matchup
        for player in self.players:
            print("-- " + player.name)
            print("Win Rate                                 : {:.2%} ({}/{})".format(
                self.wins[player.name] / (num_of_games * 2),
                self.wins[player.name], num_of_games * 2))
            print("Win Rate as white                        : {:.2%} ({}/{})".format(
                self.wins_as_white[player.name] / num_of_games,
                self.wins_as_white[player.name], num_of_games))
            print("Win Rate as black                        : {:.2%} ({}/{})".format(
                self.wins_as_black[player.name] / num_of_games,
                self.wins_as_black[player.name], num_of_games))
            print("Calculation time                         : min {:.2f}s; max {:.2f}s; avg {:.2f}s".format(
                min(self.calculation_times[player.name]),
                max(self.calculation_times[player.name]),
                (sum(self.calculation_times[player.name]) / len(self.calculation_times[player.name]))))
            round_length = self.round_length_win[player.name]
            if not round_length:
                round_length = [-1]
            print("Round length when won (lower is better)  : min {:.2f}; max {:.2f}; avg {:.2f}".format(
                min(round_length),
                max(round_length),
                (sum(round_length) / len(round_length))))
            round_length = self.round_length_loss[player.name]
            if not round_length:
                round_length = [-1]
            print("Round length when lost (higher is better): min {:.2f}; max {:.2f}; avg {:.2f}".format(
                min(round_length),
                max(round_length),
                (sum(round_length) / len(round_length))))
