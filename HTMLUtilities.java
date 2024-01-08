/**
 *	Utilities for handling HTML
 *
 *	@author Prathik Kumar
 *	@since	October 31, 2023
 */
public class HTMLUtilities {
	private enum TokenState { NONE, COMMENT, PREFORMAT }

	private TokenState currentState; // The current tokenizer state

	public HTMLUtilities() {
		currentState = TokenState.NONE; // Initialize the state to NONE
	}
	
	/**
	 *	Break the HTML string into tokens. The array returned is
	 *	exactly the size of the number of tokens in the HTML string.
	 *	Example:	HTML string = "Goodnight moon goodnight stars"
	 *				returns { "Goodnight", "moon", "goodnight", "stars" }
	 *	@param str			the HTML string
	 *	@return				the String array of tokens
	 */
	public String[] tokenizeHTMLString(String str) {
		String[] result = new String[10000];
		int index = 0;
		boolean insideTag = false;
		boolean insideWord = false;
		boolean insidePunctuation = false;
		int tagStart = -1;
		int wordStart = -1;
		int commentStart = -1;

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			// Check for comment start (<!--)
			if (c == '<' && i < str.length() - 3 && str.substring(i, i + 4).equals("<!--")) {
				currentState = TokenState.COMMENT;
				commentStart = i;
			}
			if (c == '<' && i + 4 < str.length() && str.substring(i, i + 5).equals("<pre>")) {
				currentState = TokenState.PREFORMAT;
			}
			switch (currentState) {
				case NONE:
					if (c == '<') { // Html Start Tag check
						if (insideWord) {
							if (!insidePunctuation) {
								result[index++] = str.substring(wordStart, i);
							}
							insideWord = false;
							insidePunctuation = false;
						}
						insideTag = true;
						tagStart = i;
						currentState = TokenState.NONE;
					} else if (c == '>') { // Html End Tag check
						if (insideTag) {
							result[index++] = str.substring(tagStart, i + 1);
							insideTag = false;
							currentState = TokenState.NONE;
						}
					} else if (!insideTag) { // Process contents not within html tag boundaries
						if (Character.isLetter(c) || c == '-') {
							if (!insideWord) {
								wordStart = i;
								insideWord = true;
							}
						} else if (isPunctuation(c, i, str)) {
							if (c == '.' && (i > 0 && !Character.isWhitespace(str.charAt(i - 1)))) {
								result[index++] = str.substring(wordStart, i);
							}
							result[index++] = String.valueOf(c);
							insideWord = false;
							insidePunctuation = true;
						} else if (isNumber(c, i, str)) {
							if (!insideWord) {
								wordStart = i;
								insideWord = true;
							}
						} else {
							if (insideWord) {
								if (!insidePunctuation) {
									result[index++] = str.substring(wordStart, i);
								} else {
									result[index++] = String.valueOf(c);
								}
								insideWord = false;
								insidePunctuation = false;
							}
						}
					}

					// Check for comment start (<!--)
					if (c == '<' && i < str.length() - 3 && str.substring(i, i + 4).equals("<!--")) {
						currentState = TokenState.COMMENT;
						commentStart = i;
					}
					break;

				case COMMENT:
					// Comment state
					int indexOfCommentEnd = str.indexOf("-->", commentStart);
					if (indexOfCommentEnd != -1) {
						i = indexOfCommentEnd + 2;
						currentState = TokenState.NONE;
					} else {
						i = str.length();
					}
					break;
				case PREFORMAT:
					// Preformat state
					int indexOfPreformatEnd = str.indexOf("</pre>", i);
					if (indexOfPreformatEnd != -1) {
						result[index++] = "</pre>";
						i = indexOfPreformatEnd + 5;
						currentState = TokenState.NONE;
					} else {
						result[index++] = str.substring(i);
						i = str.length(); // Move the index to the end of the line
					}
					break;
			}
		}

		if (insideWord && !insidePunctuation) {
			result[index++] = str.substring(wordStart);
		}

		String[] newResult = new String[index];
		for (int i = 0; i < index; i ++) {
			newResult[i] = result[i];
		}
		return newResult;
	}

	/**
	 * Checks if a character is a punctuation character, including '.', ',', ';', ':', '(', ')',
	 * '?', '!', '=', '~', '+', '-', and '&'. With some special handling for minus sign.
	 *
	 * @param c The character to check.
	 * @return true if the character is a punctuation character, false otherwise.
	 */
	boolean isPunctuation(char c, int i, String s) {
		String punctuationChars = ".,;:()?!=~+&-";
		if (c == '.') {
			if (i < s.length() - 1 && Character.isDigit(s.charAt(i + 1))) {
				return false;  // part of a number
			} else {
				return true;  // standalone punctuation
			}
		}
		if (c == '-') {
			if (i < s.length() - 1 && Character.isLetter((s.charAt(i + 1)))) {
				return false;
			} else {
				return true;
			}
		}
		return punctuationChars.contains(String.valueOf(c));
	}

	/**
	 * Checks if a character is part of a number, which can include digits, a decimal point,
	 * the 'e' or 'E' for scientific notation, and the plus or minus sign for positive or
	 * negative numbers.
	 *
	 * @param c The character to check.
	 * @return true if the character is part of a number, true otherwise.
	 */
	boolean isNumber(char c, int i, String s) {
		if (c == '-') {
			
			if (i < s.length() - 1 && Character.isDigit(s.charAt(i + 1))) {
				return true;  
			} else {
				return false; 
			}
		}
		return Character.isDigit(c) || c == '.' || c == 'e' || c == '+';
	}

	/**
	 *	Print the tokens in the array to the screen
	 *	Precondition: All elements in the array are valid String objects.
	 *				(no nulls)
	 *	@param tokens		an array of String tokens
	 */
	public void printTokens(String[] tokens) {
		if (tokens == null) return;
		for (int a = 0; a < tokens.length; a++) {
			if (a % 5 == 0) System.out.print("\n  ");
			System.out.print("[token " + a + "]: " + tokens[a] + " ");
		}
		System.out.println();
	}
}
