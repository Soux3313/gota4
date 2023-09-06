import subprocess
import time
import shutil
import os
import psutil
import platform
import requests
import warnings
import copy
from multiprocessing import cpu_count

warnings.filterwarnings("ignore")  # ignore warnings (mostly for the ssl warnings)

# global stuff

global open_ports
open_ports = list(range(8000, 9000))

server_jar = "Server.jar"
hostname = "localhost"
token = "hackeraccesstoken"
log_folder = "logs/"
server_startup_delay = 0.5
player_startup_delay = 1

json_header = {'Content-type': 'application/json'}

jar_folder = "current_jars/"
root_folder = "../../"


def get_newest_jars(root_folder: str, jar_folder: str):
    for x in ["KISpieler", "Server", "Beobachter"]:
        src = "{0}/{1}/build/libs/{1}.jar".format(root_folder, x)
        shutil.copy(src, jar_folder)


class PlayerInstance:
    def __init__(self, name, player_id, player_jar, logs):
        self.player_jar = player_jar
        self.name = name
        self.player_id = player_id
        self.port = open_ports.pop(0)
        if logs:
            self.log_file = open("{}Player_Log_{}.txt".format(log_folder, self.port), "w")
        else:
            self.log_file = subprocess.DEVNULL
        if platform.system() == "Windows":
            self.process = subprocess.Popen("java -jar {} -hostname {} -port {}".format(
                jar_folder + self.player_jar, hostname, self.port), stdout=self.log_file, stderr=self.log_file,
                creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
        else:
            self.process = subprocess.Popen(["java", "-jar", jar_folder + self.player_jar, "-hostname", hostname,
                                             "-port", str(self.port)], stdout=self.log_file, preexec_fn=os.setsid)
        self.active = True

    def close(self):
        parent = psutil.Process(self.process.pid)
        children = parent.children(recursive=True)
        everything_ok = True
        for child in children:
            try:
                child.kill()
            except psutil.NoSuchProcess:
                everything_ok = False
        try:
            parent.kill()
        except psutil.NoSuchProcess:
            everything_ok = False
        try:
            self.log_file.close()
        except AttributeError:
            pass
        if everything_ok:
            open_ports.insert(0, self.port)
        self.active = False


class ServerInstance:
    def __init__(self, max_concurrent_games: int, logs):
        self.current_game_id = 0
        self.port = open_ports.pop(0)
        self.running_games = []
        self.finished_games = []
        self.url = "https://{}:{}".format(hostname, self.port)
        if logs:
            self.log_file = open("{}Server_Log_{}.txt".format(log_folder, self.port), "w")
        else:
            self.log_file = subprocess.DEVNULL
        if platform.system() == "Windows":
            self.process = subprocess.Popen("java -jar {} -hostname {} -port {} -token {}".format(
                jar_folder + server_jar, hostname, self.port, token), stdout=self.log_file, stderr=self.log_file,
                creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
        else:
            self.process = subprocess.Popen(["java", "-jar", jar_folder + server_jar, "-hostname", hostname,
                                             "-port", str(self.port), "-token", token], stdout=self.log_file,
                                            start_new_session=True)
        self.active = True
        self.max_concurrent_games = max_concurrent_games

    def close(self):
        parent = psutil.Process(self.process.pid)
        children = parent.children(recursive=True)
        everything_ok = True
        for child in children:
            try:
                child.kill()
            except psutil.NoSuchProcess:
                everything_ok = False
        try:
            parent.kill()
        except psutil.NoSuchProcess:
            everything_ok = False
        try:
            self.log_file.close()
        except AttributeError:
            pass
        if everything_ok:
            open_ports.insert(0, self.port)
        self.active = False

    def check_free_slots(self, games) -> int:
        active_games = 0
        for game in games:
            if "winningPlayer" in game:
                pass
            else:
                active_games += 1
        return self.max_concurrent_games - active_games

    def connect_players(self, p1_instance: PlayerInstance, p2_instance: PlayerInstance):
        if self.check_free_slots(self.get_games()) < 1:
            return False
        json_template = '{{' \
                        '   "game" : {{' \
                        '       "players" : [' \
                        '           {{' \
                        '               "playerId" : {},' \
                        '                "name" : "{}",' \
                        '                "url"  : "https://{}:{}"' \
                        '           }},' \
                        '           {{' \
                        '               "playerId" : {},' \
                        '               "name" : "{}",' \
                        '               "url" : "https://{}:{}"' \
                        '           }}' \
                        '       ],' \
                        '       "maxTurnTime" : 600000,' \
                        '       "initialBoard" : {{' \
                        '           "gameSizeRows" : 10,' \
                        '           "gameSizeColumns" : 10,' \
                        '           "squares" : [' \
                        '               [ -1, -1, -1,  1, -1, -1,  1, -1, -1, -1],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [  1, -1, -1, -1, -1, -1, -1, -1, -1,  1],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [  0, -1, -1, -1, -1, -1, -1, -1, -1,  0],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1],' \
                        '               [ -1, -1, -1,  0, -1, -1,  0, -1, -1, -1]' \
                        '           ]' \
                        '       }}' \
                        '   }}' \
                        '}}'
        p1_id = p1_instance.player_id
        p1_name = p1_instance.name
        p1_port = p1_instance.port
        p2_id = p2_instance.player_id
        p2_name = p2_instance.name
        p2_port = p2_instance.port
        json_filled = json_template.format(p1_id, p1_name, hostname, p1_port, p2_id, p2_name, hostname, p2_port)
        r = requests.post(self.url + "/games/" + str(self.current_game_id) + "?token=" + token, data=json_filled,
                          verify=False)
        self.running_games.append(self.current_game_id)
        self.current_game_id += 1
        return True

    def get_games(self) -> dict:
        r = requests.get(self.url + "/games", verify=False)
        return r.json()["games"]

    def get_freshly_finished_games(self, games):
        freshly_finished_games = []
        for game in games:
            if "winningPlayer" in game:
                if game["gameId"] not in self.finished_games:
                    self.running_games.remove(game["gameId"])
                    self.finished_games.append(game["gameId"])
                    player1_name = game["players"][0]["name"]
                    player1_id = game["players"][0]["playerId"]
                    player2_name = game["players"][1]["name"]
                    player2_id = game["players"][1]["playerId"]
                    winning_player = game["winningPlayer"]
                    r = requests.get(self.url + "/games/" + str(game["gameId"]), params={"token": token}, verify=False)
                    turns = len(r.json()["game"]["turns"])
                    game_summary = (player1_name, player2_name, player1_id, player2_id, winning_player, turns)
                    freshly_finished_games.append(game_summary)

        return freshly_finished_games


class Tournament:
    def __init__(self, player_jars, games_per_matchup, simultaneous_matches, gameservers, debug=False, logs=False):
        assert simultaneous_matches % gameservers == 0
        self.logs = logs
        self.debug = debug
        self.simultaneous_matches = simultaneous_matches
        self.gameservers = gameservers
        self.games_per_matchup = games_per_matchup
        self.player_jars = player_jars
        self.wins = dict()
        self.wins_as_white = dict()
        self.wins_as_black = dict()
        self.round_length_win = dict()
        self.round_length_loss = dict()
        for player in range(len(self.player_jars)):
            self.wins[player] = 0
            self.wins_as_white[player] = 0
            self.wins_as_black[player] = 0
            self.round_length_win[player] = []
            self.round_length_loss[player] = []

    def c_print(self, *args, **kwargs):
        if self.debug:
            print(*args, **kwargs)

    def run(self):
        self.c_print("Generating queue...")
        queue = []
        for w in range(len(self.player_jars)):
            for b in range(len(self.player_jars)):
                if w == b:
                    continue
                for x in range(self.games_per_matchup):
                    queue.append((w, b))
        print("There will be {} matches".format(len(queue)))
        self.c_print("Starting servers...")
        servers = []
        for x in range(self.gameservers):
            servers.append(ServerInstance(int(self.simultaneous_matches / self.gameservers), logs=self.logs))
            self.c_print("Started server at {}".format(servers[-1].url))
        time.sleep(server_startup_delay)
        percentage = 100 / len(queue)
        progress = 0
        instance_id_to_instance = dict()  # a dict playerInstanceID -> playerInstance
        player_id_counter = 0
        currently_running = "start_value"
        start_time = time.time()
        while currently_running:
            if currently_running == "start_value":
                currently_running = []
            time.sleep(1)
            # get games from servers
            games = []
            for server in servers:
                games.append(server.get_games())
            # determine the free slots
            free_slots = []
            for index, server in enumerate(servers):
                free_slots += [index] * server.check_free_slots(games[index])
            # get all the fresh games
            fresh_games = []
            for index, server in enumerate(servers):
                tmp = server.get_freshly_finished_games(games[index])
                if tmp:
                    fresh_games += tmp
            # extract the finished player instances
            homeless_instances = []
            for fresh_game in fresh_games:
                homeless_instances.append((int(fresh_game[0]), fresh_game[2]))
                homeless_instances.append((int(fresh_game[1]), fresh_game[3]))
            # fill the free slots
            for free_slot in free_slots:
                try:
                    w, b = queue.pop(0)
                except IndexError:
                    break
                adopted_w = None
                adopted_b = None
                # if an old finished (homeless) instance can take over, do so
                tmp = copy.deepcopy(homeless_instances)
                for player_id, instance_id in tmp:
                    if player_id == w and not adopted_w:
                        adopted_w = instance_id_to_instance[instance_id]
                        homeless_instances.remove((player_id, instance_id))
                        self.c_print("Reused instance {} for player {}".format(instance_id, w))
                    if player_id == b and not adopted_b:
                        adopted_b = instance_id_to_instance[instance_id]
                        homeless_instances.remove((player_id, instance_id))
                        self.c_print("Reused instance {} for player {}".format(instance_id, b))
                # else create a new instance
                started_sth = False
                if not adopted_w:
                    adopted_w = PlayerInstance(str(w), player_id_counter, self.player_jars[w], logs=self.logs)
                    instance_id_to_instance[player_id_counter] = adopted_w
                    currently_running.append(adopted_w)
                    started_sth = True
                    self.c_print("Created new instance {} for player {} on port {}".format(player_id_counter, w,
                                                                                           adopted_w.port))
                    player_id_counter += 1
                if not adopted_b:
                    adopted_b = PlayerInstance(str(b), player_id_counter, self.player_jars[b], logs=self.logs)
                    instance_id_to_instance[player_id_counter] = adopted_b
                    currently_running.append(adopted_b)
                    started_sth = True
                    self.c_print("Created new instance {} for player {} on port {}".format(player_id_counter, b,
                                                                                           adopted_b.port))
                    player_id_counter += 1
                # connect players to server
                if started_sth:
                    time.sleep(player_startup_delay)
                server = servers[free_slot]
                assert server.connect_players(adopted_w, adopted_b)
                self.c_print("Connected players {}, {} to server {}".format(adopted_w.player_id, adopted_b.player_id,
                                                                            free_slot))
            # kill the homeless
            for homeless in homeless_instances:
                instance = instance_id_to_instance[homeless[1]]
                instance.close()
                instance_id_to_instance.pop(homeless[1])
                currently_running.remove(instance)
                self.c_print("Killed instance {} from player {}".format(homeless[1], homeless[0]))
            # gather the stats
            for fresh_game in fresh_games:
                player1_name, player2_name, player1_id, player2_id, winning_player, turns = fresh_game
                player1_name = int(player1_name)
                player2_name = int(player2_name)
                if winning_player == 0:
                    winner = player1_name
                    loser = player2_name
                    self.wins_as_white[winner] += 1
                else:
                    winner = player2_name
                    loser = player1_name
                    self.wins_as_black[winner] += 1
                self.wins[winner] += 1
                self.round_length_win[winner].append(turns)
                self.round_length_loss[loser].append(turns)

                progress += percentage
                print("({:05.2f}%) {}(W) vs. {}(B) => {} won in {} turns".format(progress, player1_name, player2_name,
                                                                                 winner, turns))
        self.c_print("Cleanup")
        for instance in currently_running:
            instance.close()
            instance_id_to_instance.pop(instance.player_id)
            self.c_print("Killed instance {} from player {}".format(instance.player_id, instance.name))
        currently_running = []
        for server in servers:
            server.close()
            self.c_print("Killed server on port {}".format(server.port))
        overall_time = time.time() - start_time
        self.print_report(overall_time)

    def print_report(self, overall_time):
        print("---- REPORT ----")
        num_of_games = (len(self.player_jars) - 1) * self.games_per_matchup
        for player in range(len(self.player_jars)):
            print("-- " + self.player_jars[player] + " ({})".format(player))
            print("Win Rate                                 : {:.2%} ({}/{})".format(
                self.wins[player] / (num_of_games * 2),
                self.wins[player], num_of_games * 2))
            print("Win Rate as white                        : {:.2%} ({}/{})".format(
                self.wins_as_white[player] / num_of_games,
                self.wins_as_white[player], num_of_games))
            print("Win Rate as black                        : {:.2%} ({}/{})".format(
                self.wins_as_black[player] / num_of_games,
                self.wins_as_black[player], num_of_games))
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
        print("Overall time {:.2f}s, avg game time {:.2f}s".format(overall_time, overall_time / num_of_games))


# if __name__ == "__main__":
#     get_newest_jars(root_folder, jar_folder)
#     s = ServerInstance(1)
#     p1 = PlayerInstance("p1", 1, "KISpieler.jar")
#     p2 = PlayerInstance("p2", 2, "KISpieler.jar")
#     time.sleep(1)
#     s.connect_players(p1, p2)
#     i = 0
#     while s.check_free_slots() == 0:
#         time.sleep(0.1)
#         i += 1
#     print(i)
#     print(s.get_freshly_finished_games())
#     s.close()
#     p1.close()
#     p2.close()

if __name__ == "__main__":
    #get_newest_jars(root_folder, jar_folder)
    # "KISpieler.jar"
    jars_to_test = ["KISpieler.jar", "KISpieler.jar"]
    t = Tournament(jars_to_test, 50, 12, 1, debug=True, logs=False)
    t.run()

    if cpu_count() == 16:  # stupid hack to shut down after program finished, if run on my cloud server (to save cost)
        os.system("sudo shutdown -h +5")
