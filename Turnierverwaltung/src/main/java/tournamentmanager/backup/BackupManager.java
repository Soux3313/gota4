package tournamentmanager.backup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.game.Game;
import model.jsonstruct.GameStruct;
import validation.JsonRequireRecvRecursive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class BackupManager {
	
	// filepath of the backup file alpha
	private final Path backupFilePathAlpha;
	
	 // filepath of the backup file beta
	private final Path backupFilePathBeta;
	
	// stores finished games
	private ArrayList<Game> finishedGames = new ArrayList<>();
	
	public BackupManager(String backupDirectoryPath) {
		this.backupFilePathAlpha = Path.of(backupDirectoryPath, "backup_alpha.json");
		this.backupFilePathBeta = Path.of(backupDirectoryPath, "backup_beta.json");
	}
	
	/**
	 * Returns the existing history in the backupmanager instance, does not load it from the backup files
	 */
	public ArrayList<Game> getHistory() {		
		return this.finishedGames;
	}
	
	/**
	 * Loads and parses the existing backup
	 * TODO
	 * @return null if nothing should be restored, list of games to be restored otherwise
	 */
	public ArrayList<Game> restore() {
		
		if(!this.doesAlphaBackupExist()) { // if the alpha backup does not exist, no backups have been made
			return null;
		}
		
		BackupFileStruct backupFileAlpha = null;
		boolean alphaFileCorrupt = false;
		try { 
			backupFileAlpha = this.loadBackupFile(this.backupFilePathAlpha);
		} catch(JsonSyntaxException e) {
			System.out.println("[BackupManager] Alpha file is corrupt");
			
			alphaFileCorrupt = true;
		}
		
		BackupFileStruct backupFileBeta = null;
		boolean betaFileCorrupt = false;
		try { 
			backupFileBeta = this.loadBackupFile(this.backupFilePathBeta);
		} catch(JsonSyntaxException e) {
			System.out.println("[BackupManager] Beta file is corrupt");
			
			betaFileCorrupt = true;
		}
		
		if(alphaFileCorrupt && betaFileCorrupt) {
			System.out.println("[BackupManager] Both backup files are corrupt, this needs manual attention!");
			
			System.exit(1);
			return null; // so the compiler does not complain
		}
		
		// We now know atleast one of of the backup files contains valid json syntax
		// and since we clear files before writting them it being valid json syntax implies it being a valid backup
		
		BackupFileStruct backupFileToRestoreFrom;
				
		if(alphaFileCorrupt) {
			// if backup alpha is corrupt that means we can still use backup beta
			
			System.out.println("[BackupManager] Restoring from the beta file..");
			
			backupFileToRestoreFrom = backupFileBeta;
		} else { 
			// if alpha is not corrupt we can use it
			
			System.out.println("[BackupManager] Restoring from the alpha file..");
			
			backupFileToRestoreFrom = backupFileAlpha;
		} 
		
		ArrayList<Game> gamesToRestore = new ArrayList<>();
		for(GameStruct struct : backupFileToRestoreFrom.games) {
			gamesToRestore.add(struct.intoModel());
		}
		
		this.finishedGames = gamesToRestore;
		
		return gamesToRestore;
	}
	
	/**
	 * Returns whether the alpha file even exists
	 * @return boolean
	 */
	private boolean doesAlphaBackupExist() {
		return Files.exists(backupFilePathAlpha);
	}

	/**
	 * This defines the structure of a requestBody in Json format
	 */
	static class BackupFileStruct {
		@JsonRequireRecvRecursive
		GameStruct[] games;
	}
	
	/**
	 * Parses the contents of the backup file into a backup file struct
	 * 
	 * @param filePath the path of the backup file
	 * @return an Arraylist of games contained in the backup file
	 */
	private BackupFileStruct loadBackupFile(Path filePath) throws JsonSyntaxException {	
		// Empty array as default in case try-block poops itself
		String raw = "";
		
		// Attempt to read file at filepath
		try {
			raw = Files.readString(filePath);
		} catch(IOException e) {
			System.out.println("[BackupManager] Failed to load backup file, shutting down..");
			e.printStackTrace();
			
			System.exit(1);
			return null;
		}
		
		//Make Gson do the parsing with some magic
		Gson gson = new Gson();

		return gson.fromJson(raw, BackupFileStruct.class);
	}
	
	/**
	 * Adds the game to history and then creates a backup of the history
	 * @param game
	 */
	public void storeGameInHistory(Game game) {
		this.finishedGames.add(game);
		
		this.storeHistory();
	}
	
	/**
	 * Creates a backup of the history
	 */
	private void storeHistory() {		
		BackupFileStruct backupFileStruct = new BackupFileStruct();		
		backupFileStruct.games = this.finishedGames.stream()
				.map(g -> GameStruct.fromModel(g, true))
				.toArray(GameStruct[]::new);
	
		Gson gson = new Gson();

		String jsonString = gson.toJson(gson.toJsonTree(backupFileStruct));		
		
		try {
			this.storeHistoryInFile(this.backupFilePathAlpha, jsonString);
			this.storeHistoryInFile(this.backupFilePathBeta, jsonString);
		} catch (IOException e) {
			System.out.println("[BackupManager] Error while writing history to disk");
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores the history in a file path
	 * @param filePath
	 * @param jsonString String representing the history
	 * @throws IOException 
	 */
	private void storeHistoryInFile(Path filePath, String jsonString) throws IOException {
		// first write an empty file
		Files.writeString(filePath, "", StandardCharsets.UTF_8);
		
		// then write history as json string
		Files.writeString(filePath, jsonString, StandardCharsets.UTF_8);
	}
	
	/**
	 * Deletes backup once the tournament is over
	 * @throws IOException 
	 */
	public void dumpBackup() throws IOException {
		System.out.println("[BackupManager] Dumping backup..");
		
		Files.delete(this.backupFilePathAlpha);
		Files.delete(this.backupFilePathBeta);
	}
}
