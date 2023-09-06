import random
from multiprocessing import Pool, cpu_count
import copy
from gamelogic import Player, GameField
import b64
import json

class Cache:
    def __init__(self):
        self.cache = dict()

    @staticmethod
    def _from_matrix_to_binary_string(matrix, my_queens, other_queens):
        binary_string = ""
        for x in matrix:
            for y in x:
                if y == "e":
                    binary_string += "0"
                else:
                    binary_string += "1"
        for queens in [my_queens, other_queens]:
            queen_string = ""
            for queen in sorted(queens):
                assert matrix[queen[0]][queen[1]] == "X"
                queen_string += str(queen[0]) + str(queen[1])
            leading_zeros = 0
            for char in queen_string:
                if char == "0":
                    leading_zeros += 1
                else:
                    break
            queen_string = (("0" * leading_zeros) + bin(int(queen_string))[2:]).zfill(27)
            binary_string += queen_string
        return binary_string.zfill(b64.round_up(len(binary_string)))

    @staticmethod
    def _from_binary_string_to_matrix(binary_string):
        bin_string = binary_string[-154:]
        bin_board = bin_string[:100]
        bin_my_queens = bin_string[100:127]
        bin_other_queens = bin_string[127:]
        assert bin_board + bin_my_queens + bin_other_queens == bin_string
        new_matrix = []
        i = 0
        for x in range(10):
            new_matrix.append([])
            for y in range(10):
                if bin_board[i] == "0":
                    char = "e"
                else:
                    char = "X"
                new_matrix[x].append(char)
                i += 1
        new_my_queens = []
        new_other_queens = []
        tmp = ((new_my_queens, bin_my_queens), (new_other_queens, bin_other_queens))
        for y in range(2):
            new_queens, bin_queens = tmp[y]
            queen_string = str(int(bin_queens, 2))
            while len(queen_string) < 8:
                queen_string = "0" + queen_string
            queen_string = list(map(int, list(queen_string)))
            for queen in range(int(len(queen_string) / 2)):
                new_queens.append((queen_string[queen * 2], queen_string[(queen * 2) + 1]))

        return new_matrix, tmp[0][0], tmp[1][0]

    @staticmethod
    def _matrix_to_b64(matrix, my_queens, other_queens):
        return b64.from_bin_to_b64(Cache._from_matrix_to_binary_string(matrix, my_queens, other_queens))

    @staticmethod
    def _b64_to_matrix(b):
        return Cache._from_binary_string_to_matrix(b64.from_b64_to_bin(b))

    @staticmethod
    def _from_play_to_binary_string(play):
        (x1, y1), (x2, y2), (x3, y3) = play
        cords = (x1, y1, x2, y2, x3, y3)
        cords = list(map(str, cords))
        cords = "".join(cords)
        bin_cords = bin(int(cords))[2:].zfill(24)
        return bin_cords

    @staticmethod
    def _from_binary_string_to_play(binary_string):
        binary_string = binary_string[-20:]
        cords = str(int(binary_string, 2))
        while len(cords) < 6:
            cords = "0" + cords
        cords = list(map(int, list(cords)))
        x1, y1, x2, y2, x3, y3 = cords
        return (x1, y1), (x2, y2), (x3, y3)

    @staticmethod
    def _play_to_b64(play):
        return b64.from_bin_to_b64(Cache._from_play_to_binary_string(play))

    @staticmethod
    def _b64_to_play(b):
        return Cache._from_binary_string_to_play(b64.from_b64_to_bin(b))

    def read_from_cache(self, matrix, my_queens, other_queens):
        key = self._matrix_to_b64(matrix, my_queens, other_queens)
        result = []
        for score, play in self.cache[key]:
            result.append((score, self._b64_to_play(play)))
        return result

    def write_to_cache(self, matrix, my_queens, other_queens, value):
        # expects value in the format (score, (matrix, my_queens, other_queens)) pre sorted by score
        key = self._matrix_to_b64(matrix, my_queens, other_queens)
        result = []
        for score, play in value:
            result.append((score, self._play_to_b64(play)))
        self.cache[key] = result


class AiPlayer(Player):
    def __init__(self, name, plays_calculated=5, weights=None, aggressiveness=0.5, threads=1):
        super().__init__(name)
        self.threads = threads
        self.plays_calculated = plays_calculated
        self.weights = weights
        if self.weights is None:
            self.weights = list(map(lambda x: 2 ** x, list(range(plays_calculated, 0, -1))))  # this is default
        assert len(self.weights) == self.plays_calculated
        self.aggressiveness = aggressiveness  # <0.5 is defensive, >0.5 is aggressive, 0.5 is neutral

    @staticmethod
    def all_plays(matrix, my_queens):
        all_possible_moves = []
        for i, queen in enumerate(my_queens):
            move_positions = AiPlayer.calc_all_possible_moves(queen, matrix)
            all_possible_moves.extend([(queen, position) for position in move_positions])
        all_possible_plays = []
        for position in all_possible_moves:
            arrow_spots = AiPlayer.calc_all_possible_moves(position[1], matrix, ignoring=position[0])
            all_possible_plays.extend([(*position, arrow_spot) for arrow_spot in arrow_spots])
        return all_possible_plays

    @staticmethod
    def calc_all_possible_moves(source, matrix, ignoring=(-1, -1)):
        directions = ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, 1), (-1, -1), (1, -1))
        possible_locations = []
        for direction in directions:
            cur_x = source[0]
            cur_y = source[1]
            while 0 <= cur_x < 10 and 0 <= cur_y < 10:
                cur_x += direction[0]
                cur_y += direction[1]
                if 0 <= cur_x < 10 and 0 <= cur_y < 10:
                    if matrix[cur_x][cur_y] != "e" and (cur_x, cur_y) != ignoring:
                        break
                    possible_locations.append((cur_x, cur_y))
        return possible_locations

    @staticmethod
    def create_simple_board_matrix(matrix):
        simple_board = []
        for x in range(0, 10):
            simple_board.append([])
            for y in range(0, 10):
                char = "e"
                if matrix[x][y] != "e":
                    char = "X"
                simple_board[x].append(char)
        return simple_board

    @staticmethod
    def clone_and_apply(matrix, my_queens, other_queens, play):
        old_queen = play[0]
        new_queen = play[1]
        new_arrow = play[2]
        copied_matrix = copy.deepcopy(matrix)
        copied_matrix[old_queen[0]][old_queen[1]] = "e"
        copied_matrix[new_queen[0]][new_queen[1]] = "X"
        copied_matrix[new_arrow[0]][new_arrow[1]] = "X"
        my_queens = copy.deepcopy(my_queens)
        other_queens = copy.deepcopy(other_queens)
        if old_queen in my_queens:
            my_queens[my_queens.index(old_queen)] = new_queen
        elif old_queen in other_queens:
            other_queens[other_queens.index(old_queen)] = new_queen
        else:
            print(old_queen)
            print(my_queens)
            print(other_queens)
            return -1
        return copied_matrix, my_queens, other_queens

    def evaluate_simple_board_matrix(self, queens, board):
        directions = ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, 1), (-1, -1), (1, -1))
        store = [queens]
        for i in range(0, self.plays_calculated + 1):
            found_positions = []
            for start_position in store[i]:
                for direction in directions:
                    cur_x = start_position[0]
                    cur_y = start_position[1]
                    while 0 <= cur_x < 10 and 0 <= cur_y < 10:
                        cur_x += direction[0]
                        cur_y += direction[1]
                        if 0 <= cur_x < 10 and 0 <= cur_y < 10:
                            char = board[cur_x][cur_y]
                            if char == "X":
                                break
                            if char == "e":
                                board[cur_x][cur_y] = str(i + 1)
                                found_positions.append((cur_x, cur_y))
            store.append(found_positions)
        weighted_sum = 0
        for i in range(1, self.plays_calculated + 1):
            weighted_sum += self.weights[i - 1] * len(store[i])
        return weighted_sum

    def evaluate_play(self, play, matrix, my_queens, other_queens):
        board1, my_queens1, other_queens1 = self.clone_and_apply(matrix, my_queens, other_queens, play)
        board2, my_queens2, other_queens2 = self.clone_and_apply(matrix, my_queens, other_queens, play)
        my_score = self.evaluate_simple_board_matrix(my_queens1, board1)
        other_score = self.evaluate_simple_board_matrix(other_queens2, board2)
        total_score = (-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score
        return int(total_score)

    def cache_test(self, gamefield, color, cache):
        my_queens = []
        other_queens = []
        if color == "b":
            my_queens = gamefield.b
            other_queens = gamefield.w
        elif color == "w":
            my_queens = gamefield.w
            other_queens = gamefield.b
        matrix = self.create_simple_board_matrix(gamefield.to_board_matrix())
        try:
            result = cache.read_from_cache(matrix, my_queens, other_queens)
            print("CACHE HIT")
            return result
        except KeyError:
            all_plays = self.all_plays(matrix, my_queens)
            all_possible_plays_scored = []
            for play in all_plays:
                all_possible_plays_scored.append((self.evaluate_play(play, matrix, my_queens, other_queens), play))
            all_possible_plays_scored.sort(key=lambda x: x[0], reverse=True)
            cache.write_to_cache(matrix, my_queens, other_queens, all_possible_plays_scored)
            return all_possible_plays_scored


matrix = [["e", "e", "e", "X", "e", "e", "X", "e", "e", "e"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["X", "e", "e", "e", "e", "e", "e", "e", "e", "X"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["X", "e", "e", "e", "e", "e", "e", "e", "e", "X"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["e", "e", "e", "e", "e", "e", "e", "e", "e", "e"],
          ["e", "e", "e", "X", "e", "e", "X", "e", "e", "e"]]

w0, w1, w2, w3 = (6, 0), (9, 3), (9, 6), (6, 9)
b0, b1, b2, b3 = (3, 0), (0, 3), (0, 6), (3, 9)
my_queens = [w0, w1, w2, w3]
other_queens = [b0, b1, b2, b3]

stored_bs64_board = Cache._matrix_to_b64(matrix, my_queens, other_queens)
loaded_matrix, loaded_my_queens, loaded_other_queens = Cache._b64_to_matrix(stored_bs64_board)
print(stored_bs64_board)
print(loaded_matrix, loaded_my_queens, loaded_other_queens, sep="\n")

play = ((6, 0), (3, 3), (3, 6))
stored_bs64_play = Cache._play_to_b64(play)
loaded_play = Cache._b64_to_play(stored_bs64_play)
print(stored_bs64_play)
print(loaded_play)

ai = AiPlayer("V1 2^x / 0.8", aggressiveness=0.8, plays_calculated=5, threads=1)
cache = Cache()
result = ai.cache_test(GameField(), "w", cache)
print(cache.cache)
result = ai.cache_test(GameField(), "w", cache)
print(result)
print(json.dumps(cache.cache))
