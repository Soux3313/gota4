import random
from multiprocessing import Pool, cpu_count
import copy
from gamelogic import Player


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

    def play(self, gamefield, color):
        my_queens = []
        if color == "b":
            my_queens = gamefield.b
        elif color == "w":
            my_queens = gamefield.w
        all_possible_moves = []
        for i, queen in enumerate(my_queens):
            move_positions = AiPlayer.calc_all_possible_moves(queen, gamefield)
            all_possible_moves.extend([(color + str(i), position) for position in move_positions])
        all_possible_plays = []
        for position in all_possible_moves:
            arrow_spots = AiPlayer.calc_all_possible_moves(position[1], gamefield,
                                                           ignoring=my_queens[int(position[0][1:])])
            all_possible_plays.extend([(*position, arrow_spot) for arrow_spot in arrow_spots])
        all_possible_plays_scored = []
        if self.threads < 2:
            for play in all_possible_plays:
                all_possible_plays_scored.append((self.evaluate_play(play, gamefield, color), play))
        else:
            pool = Pool(processes=self.threads)
            args = [(play, gamefield, color) for play in all_possible_plays]
            scores = pool.starmap(self.evaluate_play, args)
            pool.close()
            all_possible_plays_scored = zip(scores, all_possible_plays)
        highest_scoring = []
        highscore = None
        for score, play in all_possible_plays_scored:
            if highscore is None or score > highscore:
                highscore = score
                highest_scoring = [play]
            elif score == highscore:
                highest_scoring.append(play)
        choice = random.choice(highest_scoring)
        queen = choice[0]
        move_x, move_y = choice[1][0], choice[1][1]
        arrow_x, arrow_y = choice[2][0], choice[2][1]
        return queen, move_x, move_y, arrow_x, arrow_y, highscore

    def evaluate_play(self, play, gamefield, color):
        my_queens = []
        other_queens = []
        if color == "b":
            my_queens = copy.deepcopy(gamefield.b)
            other_queens = copy.deepcopy(gamefield.w)
        elif color == "w":
            my_queens = copy.deepcopy(gamefield.w)
            other_queens = copy.deepcopy(gamefield.b)
        old_queen = gamefield.find_piece(play[0])
        for x in range(len(my_queens)):
            if my_queens[x] == old_queen:
                my_queens[x] = play[1]
        my_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        other_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        my_score = self.evaluate_simple_board_matrix(my_queens, my_board)
        other_score = self.evaluate_simple_board_matrix(other_queens, other_board)
        total_score = (-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score
        return total_score

    @staticmethod
    def create_simple_board_matrix(gamefield, play=(-1, (-1, -1), (-1, -1))):
        old_queen = gamefield.find_piece(play[0])
        new_queen = play[1]
        new_arrow = play[2]
        simple_board = []
        for x in range(0, 10):
            simple_board.append([])
            for y in range(0, 10):
                char = "e"
                if (x, y) in gamefield.w + gamefield.b + gamefield.arrows:
                    char = "X"
                simple_board[x].append(char)
        simple_board[old_queen[0]][old_queen[1]] = "e"
        simple_board[new_queen[0]][new_queen[1]] = "X"
        simple_board[new_arrow[0]][new_arrow[1]] = "X"
        return simple_board

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

    @staticmethod
    def calc_all_possible_moves(source, gamefield, ignoring=(-1, -1)):
        directions = ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, 1), (-1, -1), (1, -1))
        board = gamefield.to_board_matrix()
        possible_locations = []
        for direction in directions:
            cur_x = source[0]
            cur_y = source[1]
            while 0 <= cur_x < 10 and 0 <= cur_y < 10:
                cur_x += direction[0]
                cur_y += direction[1]
                if 0 <= cur_x < 10 and 0 <= cur_y < 10:
                    if board[cur_x][cur_y] != "e" and (cur_x, cur_y) != ignoring:
                        break
                    possible_locations.append((cur_x, cur_y))
        return possible_locations


class AiPlayerV2(AiPlayer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    @staticmethod
    def secondary_weights(n, x):
        return ((1 / n) * x) + (5 / n)

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
                            elif char == "e":
                                board[cur_x][cur_y] = (i + 1, 1)
                                found_positions.append((cur_x, cur_y))
                            elif char[0] == i + 1:
                                board[cur_x][cur_y] = (i + 1, char[1] + 1)
            store.append(found_positions)
        weighted_sum = 0
        for i in range(1, self.plays_calculated + 1):
            weighted_sum += self.weights[i - 1] * len(store[i])
        for x in board:
            for char in x:
                if char == "X" or char == "e":
                    continue
                weighted_sum += self.secondary_weights(char[0], char[1])
        return weighted_sum


class AiPlayerV3(AiPlayer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def evaluate_play(self, play, gamefield, color):
        my_queens = []
        other_queens = []
        if color == "b":
            my_queens = gamefield.b
            other_queens = gamefield.w
        elif color == "w":
            my_queens = gamefield.w
            other_queens = gamefield.b
        old_queen = gamefield.find_piece(play[0])
        for x in range(len(my_queens)):
            if my_queens[x] == old_queen:
                my_queens[x] = play[1]
        my_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        other_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        self.evaluate_simple_board_matrix(my_queens, my_board)
        self.evaluate_simple_board_matrix(other_queens, other_board)
        my_score = 0
        other_score = 0
        for x in range(10):
            for y in range(10):
                if not my_board[x][y].isnumeric():
                    my_board[x][y] = 999
                if not other_board[x][y].isnumeric():
                    other_board[x][y] = 999
                if int(my_board[x][y]) < int(other_board[x][y]):
                    my_score += 1
                elif int(my_board[x][y]) > int(other_board[x][y]):
                    other_score += 1
        total_score = (-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score
        return total_score


class AiPlayerV4(AiPlayer):
    def __init__(self, depth_slider, backup, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.depth_slider = depth_slider
        self.backup = backup

    @staticmethod
    def all_plays(matrix, my_queens):
        all_possible_moves = []
        for i, queen in enumerate(my_queens):
            move_positions = AiPlayerV4.calc_all_possible_moves(queen, matrix)
            all_possible_moves.extend([(queen, position) for position in move_positions])
        all_possible_plays = []
        for position in all_possible_moves:
            arrow_spots = AiPlayerV4.calc_all_possible_moves(position[1], matrix, ignoring=position[0])
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
    def modify_simple_board_matrix(matrix, my_queens, other_queens, play):
        old_queen = play[0]
        new_queen = play[1]
        new_arrow = play[2]
        copied_matrix = copy.deepcopy(matrix)
        copied_matrix[old_queen[0]][old_queen[1]] = "e"
        copied_matrix[new_queen[0]][new_queen[1]] = "X"
        copied_matrix[new_arrow[0]][new_arrow[1]] = "X"
        if old_queen in my_queens:
            my_queens = copy.deepcopy(my_queens)
            my_queens[my_queens.index(old_queen)] = new_queen
        elif old_queen in other_queens:
            other_queens = copy.deepcopy(other_queens)
            other_queens[other_queens.index(old_queen)] = new_queen
        else:
            print(old_queen)
            print(my_queens)
            print(other_queens)
            return -1
        return copied_matrix, my_queens, other_queens

    def calc_depth(self, judge):
        depth = 0
        for index, threshold in enumerate(self.depth_slider):
            if judge < threshold:
                depth = index + 1
        return depth

    def play(self, gamefield, color):
        my_queens = []
        other_queens = []
        if color == "b":
            my_queens = gamefield.b
            other_queens = gamefield.w
        elif color == "w":
            my_queens = gamefield.w
            other_queens = gamefield.b
        matrix = self.create_simple_board_matrix(gamefield.to_board_matrix())
        all_turns = self.all_plays(matrix, my_queens)
        all_other_turns = self.all_plays(matrix, other_queens)
        depth = self.calc_depth(len(all_turns) + len(all_other_turns))
        if depth == 0:
            return self.backup.play(gamefield, color)
        maxVal = float("-inf")
        alpha = float("-inf")
        beta = float("inf")
        for turn in all_turns:
            temp_matrix, temp_my_queens, temp_other_queens = self.modify_simple_board_matrix(matrix, my_queens,
                                                                                             other_queens, turn)
            temp_result = self.minimax(temp_matrix, temp_my_queens, temp_other_queens, alpha, beta, False, depth)
            if temp_result > maxVal:
                maxVal = temp_result
                bestTurn = turn
            alpha = max(alpha, maxVal)
            if beta <= alpha:
                break

        choice = bestTurn
        queen = choice[0]
        queen = color + str(my_queens.index(queen))
        move_x, move_y = choice[1][0], choice[1][1]
        arrow_x, arrow_y = choice[2][0], choice[2][1]
        return queen, move_x, move_y, arrow_x, arrow_y

    def minimax(self, matrix, my_queens, other_queens, alpha, beta, maximizing_player, depth):
        all_turns = []
        if maximizing_player:
            all_turns = self.all_plays(matrix, my_queens)
        else:
            all_turns = self.all_plays(matrix, other_queens)
        if depth == 0 or len(all_turns) == 0:
            matrix1 = copy.deepcopy(matrix)
            matrix2 = copy.deepcopy(matrix)
            if maximizing_player:
                my_score = self.evaluate_simple_board_matrix(other_queens, matrix1)
                other_score = self.evaluate_simple_board_matrix(my_queens, matrix2)
                total_score = -((-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score)
            else:
                my_score = self.evaluate_simple_board_matrix(my_queens, matrix1)
                other_score = self.evaluate_simple_board_matrix(other_queens, matrix2)
                total_score = (-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score

            return total_score
        if maximizing_player:
            maxVal = float("-inf")
            for turn in all_turns:
                temp_matrix, temp_my_queens, temp_other_queens = self.modify_simple_board_matrix(matrix, my_queens,
                                                                                                 other_queens, turn)
                maxVal = max(maxVal, self.minimax(temp_matrix, temp_my_queens, temp_other_queens, alpha, beta, False,
                                                  depth - 1))
                alpha = max(alpha, maxVal)
                if beta <= alpha:
                    break
            return maxVal
        else:
            minVal = float("inf")
            for turn in all_turns:
                temp_matrix, temp_my_queens, temp_other_queens = self.modify_simple_board_matrix(matrix, my_queens,
                                                                                                 other_queens, turn)
                minVal = min(minVal,
                             self.minimax(temp_matrix, temp_my_queens, temp_other_queens, alpha, beta, True, depth - 1))
                beta = min(beta, minVal)
                if beta <= alpha:
                    break
            return minVal


class AiPlayerOLD(AiPlayer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def evaluate_play(self, play, gamefield, color):
        my_queens = []
        other_queens = []
        if color == "b":
            my_queens = gamefield.b
            other_queens = gamefield.w
        elif color == "w":
            my_queens = gamefield.w
            other_queens = gamefield.b
        my_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        other_board = AiPlayer.create_simple_board_matrix(gamefield, play)
        my_score = self.evaluate_simple_board_matrix(my_queens, my_board)
        other_score = self.evaluate_simple_board_matrix(other_queens, other_board)
        total_score = (-self.aggressiveness + 1) * my_score - self.aggressiveness * other_score
        return total_score


class AIPlayerSwitch(Player):
    def __init__(self, plays_calculated=5, threads=1, *args, **kwargs):
        super().__init__(*args, **kwargs)
        big_quadratic = list(map(lambda x: x ** 3, list(range(5, 0, -1))))
        self.black = AiPlayer("V1 2^x / 0.8", aggressiveness=0.8, plays_calculated=plays_calculated, threads=threads)
        self.white = AiPlayer("V1 x^3 / 0.9", aggressiveness=0.9, weights=big_quadratic,
                              plays_calculated=plays_calculated, threads=threads)

    def play(self, gamefield, color):
        if color == "w":
            return self.white.play(gamefield, color)
        elif color == "b":
            return self.black.play(gamefield, color)


class AIPlayerRandom(AiPlayer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def play(self, gamefield, color):
        my_queens = []
        if color == "b":
            my_queens = gamefield.b
        elif color == "w":
            my_queens = gamefield.w
        all_possible_moves = []
        for i, queen in enumerate(my_queens):
            move_positions = AiPlayer.calc_all_possible_moves(queen, gamefield)
            all_possible_moves.extend([(color + str(i), position) for position in move_positions])
        all_possible_plays = []
        for position in all_possible_moves:
            arrow_spots = AiPlayer.calc_all_possible_moves(position[1], gamefield,
                                                           ignoring=my_queens[int(position[0][1:])])
            all_possible_plays.extend([(*position, arrow_spot) for arrow_spot in arrow_spots])
        choice = random.choice(all_possible_plays)
        queen = choice[0]
        move_x, move_y = choice[1][0], choice[1][1]
        arrow_x, arrow_y = choice[2][0], choice[2][1]
        return queen, move_x, move_y, arrow_x, arrow_y


