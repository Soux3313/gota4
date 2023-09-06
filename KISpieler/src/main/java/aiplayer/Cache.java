package aiplayer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;

public class Cache {

	private final int boardSizeX;
	private final int boardSizeY;
	private final int numberOfQueens;

	private final int lengthMatrix;
	private final int lenCordX;
	private final int lenCordY;
	private final int lengthQueens;

	private final int lengthBoard;
	private final int lengthBoardPadded;

	private final int lengthTurn;
	private final int lengthTurnPadded;

	public Cache(int boardSizeX, int boardSizeY, int numberOfQueens){
		this.boardSizeX = boardSizeX;
		this.boardSizeY = boardSizeY;
		this.numberOfQueens = numberOfQueens;

		this.lengthMatrix = boardSizeX * boardSizeY;
		// just in case we use a filed bigger than 10x10, coordinates would be bigger
		this.lenCordX = String.valueOf(boardSizeX-1).length();
		this.lenCordY = String.valueOf(boardSizeY-1).length();

		int lenOfQueenCords = this.lenCordX + lenCordY;
		this.lengthQueens = fitIntoBinary(lenOfQueenCords*numberOfQueens);

		this.lengthBoard = this.lengthMatrix + (2 * this.lengthQueens);
		this.lengthBoardPadded = roundUpto(this.lengthBoard, 8);

		this.lengthTurn = fitIntoBinary(lenOfQueenCords * 3);
		this.lengthTurnPadded = roundUpto(this.lengthTurn, 8);

	}

	private String fromBoardToBinary(int[][] matrix, int[][] myQueens, int[][] otherQueens){
		StringBuilder result = new StringBuilder();
		for(int x=0; x<this.boardSizeX; x++){
			for(int y=0; y<this.boardSizeY; y++){
				if(matrix[x][y] == -1){
					result.append("0");
				}else{
					result.append("1");
				}
			}
		}
		assert result.length() == this.lengthMatrix;
		for(int[][] queens : new int[][][] {myQueens, otherQueens}){
			StringBuilder queenStr = new StringBuilder();
			int[][] sortedQueens = queens.clone();
			Arrays.sort(sortedQueens, Comparator.comparingInt(o -> o[0]));
			for(int[] queen : sortedQueens){
				assert matrix[queen[0]][queen[1]] != -1;
				queenStr.append(StringUtils.leftPad(String.valueOf(queen[0]), this.lenCordX, "0"));
				queenStr.append(StringUtils.leftPad(String.valueOf(queen[1]), this.lenCordY, "0"));
			}
			long queenInt = Long.parseLong(queenStr.toString());
			String queenBin = Long.toBinaryString(queenInt);
			queenBin = StringUtils.leftPad(queenBin, this.lengthQueens, "0");
			result.append(queenBin);
		}
		return StringUtils.leftPad(result.toString(), this.lengthBoardPadded, "0");

	}

	private int[][][] fromBinaryToBoard(String b){
		String matrixStr = b.substring(this.lengthBoardPadded-this.lengthBoard,
				this.lengthBoardPadded-this.lengthBoard+this.lengthMatrix);
		String myQueensString = b.substring(this.lengthBoardPadded-this.lengthBoard+this.lengthMatrix,
				this.lengthBoardPadded-this.lengthBoard+this.lengthMatrix+this.lengthQueens);
		String otherQueenString = b.substring(this.lengthBoardPadded-this.lengthBoard+this.lengthMatrix+this.lengthQueens,
				this.lengthBoardPadded-this.lengthBoard+this.lengthMatrix+this.lengthQueens+this.lengthQueens);
		int[][] matrix = new int[this.boardSizeX][this.boardSizeY];
		int i = 0;
		for(int x=0; x<this.boardSizeX; x++) {
			for (int y = 0; y < this.boardSizeY; y++) {
				if(matrixStr.charAt(i)=='0'){
					matrix[x][y] = -1;
				}else {
					matrix[x][y] = -2;
				}
				i++;
			}
		}
		String[] bothQueensStr = new String[] {myQueensString, otherQueenString};
		int[][][] bothQueens = new int[2][this.numberOfQueens][2];
		for(int j = 0; j<2; j++){
			String queenString = new BigInteger(bothQueensStr[j], 2).toString();
			queenString = StringUtils.leftPad(queenString, (this.lenCordX+this.lenCordX)*this.numberOfQueens, "0");
			int charIndex = 0;
			int queenIndex = 0;
			while (charIndex<queenString.length()){
				bothQueens[j][queenIndex][0] = Integer.parseInt(queenString.substring(charIndex, charIndex+this.lenCordX));
				charIndex += this.lenCordX;
				bothQueens[j][queenIndex][1] = Integer.parseInt(queenString.substring(charIndex, charIndex+this.lenCordY));
				charIndex += this.lenCordY;
				assert matrix[bothQueens[j][queenIndex][0]][bothQueens[j][queenIndex][1]] == -2;
				queenIndex++;
			}
		}
		return new int[][][] {matrix, bothQueens[0], bothQueens[1]};
	}

	public String boardToB64(int[][] matrix, int[][] myQueens, int[][] otherQueens){
		return binToB64(fromBoardToBinary(matrix, myQueens, otherQueens));
	}

	public int[][][] b64ToBoard(String b){
		return fromBinaryToBoard(b64ToBin(b));
	}

	private String fromTurnToBinary(int[] turn){
		StringBuilder turnStr = new StringBuilder();
		for(int i = 0; i<3; i++){
			turnStr.append(StringUtils.leftPad(String.valueOf(turn[i*2]), this.lenCordX, "0"));
			turnStr.append(StringUtils.leftPad(String.valueOf(turn[i*2+1]), this.lenCordY, "0"));
		}
		String result = turnStr.toString();
//        while(result.startsWith("0")){
//            result =  result.substring(1);
//        }
		return StringUtils.leftPad(Long.toBinaryString(Long.parseLong(result)), this.lengthTurnPadded, "0");
	}

	private int[] fromBinaryToTurn(String b){
		String turnBin = b.substring(this.lengthTurnPadded-this.lengthTurn);
		int charIndex = 0;
		int cordIndex = 0;
		String turnStr = StringUtils.leftPad(new BigInteger(turnBin,2).toString(), 3*(this.lenCordX+this.lenCordY), "0");
		int[] result = new int[6];
		while(charIndex<turnStr.length()){
			result[cordIndex] = Integer.parseInt(turnStr.substring(charIndex, charIndex+this.lenCordX));
			charIndex += this.lenCordX;
			cordIndex++;
			result[cordIndex] = Integer.parseInt(turnStr.substring(charIndex, charIndex+this.lenCordY));
			charIndex += this.lenCordY;
			cordIndex++;
		}
		return result;
	}

	public String turnToB64(int[] turn){
		return binToB64(fromTurnToBinary(turn));
	}

	public int[] b64ToTurn(String b){
		return fromBinaryToTurn(b64ToBin(b));
	}

	private int fitIntoBinary(int size){
		int i = 0;
		BigInteger cur = BigInteger.valueOf(1);
		while(String.valueOf(cur).length() <= size){
			cur = cur.multiply(BigInteger.valueOf(2));
			i++;
		}
		return i;
	}

	private int roundUpto(int x, int b){
		return (x + b - 1) - ((x + b - 1) % b);
	}

	public static String binToB64(String bin){
		if(bin.length() == 0){
			return "";
		}
		assert bin.length() % 8 == 0;
		byte[] bytes = new BigInteger(bin, 2).toByteArray();
		// no idea why this needs to be done, seems to work tho
		if(bin.charAt(0) == '1'){
			bytes = ArrayUtils.remove(bytes, 0);
		}
		while(bytes.length < bin.length()/8){
			bytes = ArrayUtils.insert(0, bytes, (byte) 0);
		}

		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(bytes);
	}

	public static String b64ToBin(String b64){
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] bytes = decoder.decode(b64);
		StringBuilder result = new StringBuilder();
		for (byte aByte : bytes) {
			result.append(String.format("%8s", Integer.toBinaryString(aByte & 0xFF)).replace(" ", "0"));
		}
		return result.toString();
	}

}
