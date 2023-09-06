from ki import *
from gamelogic import *
import os

if __name__ == "__main__":
    threads = 1
    print("Running on {} threads".format(threads))
    big_quadratic = list(map(lambda x: x ** 3, list(range(5, 0, -1))))
    smol_exp = list(map(lambda x: 2 ** x, list(range(5, 0, -1))))
    linear = list(map(lambda x: x, list(range(5, 0, -1))))

    p0 = AIPlayerSwitch(plays_calculated=5, threads=threads, name="The Switcher 3: Wild Hunt")
    p1 = AiPlayerV4(depth_slider=[400, 200, 100, 50, 25, 10], backup=p0, name="Happy Tree Friend", aggressiveness=0.8,
                    weights=None, plays_calculated=5, threads=threads)
    players_to_test = [p0, p1]
    t = TournamentMP(threads=cpu_count(), games_per_matchup=45, players=players_to_test)
    t.run()
    if cpu_count() == 16:  # stupid hack to shut down after program finished, if run on my cloud server (to save cost)
        os.system("sudo shutdown -h +5")

# if __name__ == "__main__":
#     threads = cpu_count()
#     p0 = AIPlayerSwitch(plays_calculated=5, threads=8, name="The Switcher 3: Wild Hunt")
#     #p1 = AIPlayerSwitch(plays_calculated=5, threads=8, name="The Switcher 3: Wild Hunt another")
#     #p1 = HumanPlayer("Me")
#     p1 = AiPlayerV4(depth_slider=[400, 200, 100, 50, 25], backup=p0, name="Happy Tree Friend", aggressiveness=0.8,
#                     weights=None, plays_calculated=5, threads=threads)
#
#     game = Game(p0, p1, cli_output=True)
#     winner, rounds, times1, times2, overall_time = game.start_gameloop()
#     print("{} wins in {} rounds.".format(winner.name, rounds))
#     print("Player1 max time {:.3f}s, avg time {:.3f}s".format(max(times1), sum(times1) / len(times1)))
#     print("Player2 max time {:.3f}s, avg time {:.3f}s".format(max(times2), sum(times2) / len(times2)))
#     print("Overall time {:.3f}s".format(overall_time))
