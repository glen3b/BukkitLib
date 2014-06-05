package me.pagekite.glen3b.library.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

/**
 * A class which rotates through text to keep it within a specific length limit.
 * All {@link CharSequence} methods are performed regarding the trimmed text.
 * @author Glen Husman
 */
public class TextCycler implements CharSequence {

	protected final String _prefix;
	protected final String _originalText;
	protected final int _trimLength;
	protected int _currentTrimTick = -1;
	/**
	 * The possible value of the text cycler, <em>including the prefix</em>. Should be created at load time.
	 */
	protected String[] _trimPossibilities = null;

	/**
	 * Creates a text cycler with the given text.
	 * @param text The untrimmed text of the cycler.
	 * @param trimLength The length of the trimmed text.
	 */
	public TextCycler(String text, int trimLength){
		this(null, text, trimLength);
	}

	/**
	 * Creates a text cycler with the given text and an uncycled prefix.
	 * @param prefix The constant prefix of the text.
	 * @param text The untrimmed text of the cycler.
	 * @param trimLength The length of the trimmed text.
	 */
	public TextCycler(String prefix, String text, int trimLength){
		if(prefix == null){
			prefix = StringUtils.EMPTY;
		}

		Validate.notEmpty(text, "Text must be specified.");
		Validate.isTrue(trimLength > 0, "The length to trim to must be positive.");

		_trimLength = trimLength;
		_originalText = text.trim();
		_prefix = prefix.trim();
		tick(); // Initialize arrays
	}
	
	/**
	 * Gets the untrimmed constant prefix to the text in this cycler.
	 * @return A non-null string representing the prefix to the cycled text.
	 */
	public String getPrefix(){
		return _prefix;
	}
	
	/**
	 * Gets the untrimmed text in this cycler which is cycled.
	 * @return A non-null string representing the original text.
	 */
	public String getText(){
		return _originalText;
	}

	/**
	 * Ticks the text cycler, incrementing the start position of the text.
	 * @return The <em>new</em> value of the text cycler, including the prefix. This value is also assigned to the appropriate variables, and can be retrieved by the appropriate methods.
	 */
	public String tick(){
		if(_trimPossibilities == null){
			if(_originalText.length() + _prefix.length() <= _trimLength){
				// Cache the single variable
				_trimPossibilities = new String[]{StringUtils.rightPad(_prefix + _originalText, _trimLength)};
			}else{
				List<String> entrySet = new ArrayList<String>(); // This list does NOT contain prefixes
				int unprefixedLen = _trimLength - _prefix.length();
				int index = 0;
				do{
					if(index + unprefixedLen <= _originalText.length()){
						entrySet.add(_originalText.substring(index, index + unprefixedLen));
					}else if(index <= _originalText.length()){
						entrySet.add(StringUtils.rightPad(_originalText.substring(index), unprefixedLen));
					}else if(index - unprefixedLen < _originalText.length()){
						entrySet.add(StringUtils.leftPad(_originalText.substring(0, index - unprefixedLen), unprefixedLen));
					}else{
						break;
					}
					index++;
				}while(/*entrySet.size() < 2 || !entrySet.get(entrySet.size() - 1).equals(entrySet.get(0))*/true);
				_trimPossibilities = new String[entrySet.size()];
				for(int i = 0; i < _trimPossibilities.length; i++){
					_trimPossibilities[i] = _prefix + entrySet.get(i);
					if(_trimPossibilities[i].length() > _trimLength){
						Bukkit.getLogger().log(Level.WARNING, "[TextCycler: Debug] String has length greater than maximum! Value: \"" + _trimPossibilities[i] + "\" Index: " + i + " Maxlength:  " + _trimLength);
					}
				}
			}
		}
		//	_originalText.substring(_currentTrimIndex, _currentTrimIndex + (_trimLength - _prefix.length()));


		return _trimPossibilities[++_currentTrimTick % _trimPossibilities.length];
	}

	@Override
	public int length() {
		return _trimLength;
	}

	@Override
	public char charAt(int index) {
		return _trimPossibilities[_currentTrimTick % _trimPossibilities.length].charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return _trimPossibilities[_currentTrimTick % _trimPossibilities.length].subSequence(start, end);
	}

	@Override
	public String toString(){
		return _trimPossibilities[_currentTrimTick % _trimPossibilities.length];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _currentTrimTick;
		result = prime * result
				+ ((_originalText == null) ? 0 : _originalText.hashCode());
		result = prime * result + ((_prefix == null) ? 0 : _prefix.hashCode());
		result = prime * result + _trimLength;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TextCycler)) {
			return false;
		}
		TextCycler other = (TextCycler) obj;
		if (_currentTrimTick != other._currentTrimTick) {
			return false;
		}
		if (_originalText == null) {
			if (other._originalText != null) {
				return false;
			}
		} else if (!_originalText.equals(other._originalText)) {
			return false;
		}
		if (_prefix == null) {
			if (other._prefix != null) {
				return false;
			}
		} else if (!_prefix.equals(other._prefix)) {
			return false;
		}
		if (_trimLength != other._trimLength) {
			return false;
		}
		return true;
	}

}
