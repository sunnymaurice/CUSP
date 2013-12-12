package tw.com.charder.cusp_operator;

public enum KeypadButton {	
	CLEAR("C",KeypadButtonCategory.CLEAR)
	, ZERO("0",KeypadButtonCategory.NUMBER)
	, ONE("1",KeypadButtonCategory.NUMBER)
	, TWO("2",KeypadButtonCategory.NUMBER)
	, THREE("3",KeypadButtonCategory.NUMBER)
	, FOUR("4",KeypadButtonCategory.NUMBER)
	, FIVE("5",KeypadButtonCategory.NUMBER)
	, SIX("6",KeypadButtonCategory.NUMBER)
	, SEVEN("7",KeypadButtonCategory.NUMBER)
	, EIGHT("8",KeypadButtonCategory.NUMBER)
	, NINE("9",KeypadButtonCategory.NUMBER)
	, DECIMAL_SEP(".",KeypadButtonCategory.NUMBER)
	/*, ADD("ADD",KeypadButtonCategory.OPERATOR)*/	
	, MULTIPLY(" x ",KeypadButtonCategory.OPERATOR)	
	, TARE(">T<",KeypadButtonCategory.SCALE_FUNC)
	, RST_ZERO(">O<",KeypadButtonCategory.SCALE_FUNC)	
	, CALCULATE("<--",KeypadButtonCategory.RESULT)
	, DUMMY("",KeypadButtonCategory.DUMMY);

	CharSequence mText; // Display Text
	KeypadButtonCategory mCategory;
	
	KeypadButton(CharSequence text,KeypadButtonCategory category) {
		mText = text;
		mCategory = category;
	}

	public CharSequence getText() {
		return mText;
	}
}
