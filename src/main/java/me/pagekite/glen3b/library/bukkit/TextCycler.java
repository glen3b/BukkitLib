package me.pagekite.glen3b.library.bukkit;

import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

/**
 * A class which rotates through text to keep it within a specific length limit.
 * @author Glen Husman
 */
public final class TextCycler {

	protected final String _prefix;
	protected final String _originalText;
	protected final int _trimLength;
	protected int _currentTrimIndex = 0;
	/**
	 * Used to cache the string instance, which (if constant and applicabgle) will be used instead of rebuilding the builder each tick.
	 */
	private String _cachedValue = null;

	/**
	 * Creates a text cycler with the given text.
	 * @param text The untrimmed text of the cycler.
	 * @param trimLength The length of the trimmed text.
	 */
	public TextCycler(String text, int trimLength){
		this(null, text, trimLength);
	}
	
	/**
	 * Gets the length to which text is trimmed by this cycler.
	 * @return The trim length of this text cycler instance.
	 */
	public int getTrimLength(){
		return _trimLength;
	}

	/**
	 * Creates a text cycler with the given text and an uncycled prefix.
	 * <p>
	 * Note that the text cycler does not automatically append spacing to the body text string.
	 * The caller may wish to do so, such as via a call to {@link StringUtils#leftPad(String, int)}.
	 * If this is called with the desired pad length on the input string, the padding affect will occur.
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
		_originalText = text;
		_prefix = prefix.trim();
		if (_originalText.length() <= _trimLength - _prefix.length()) {
			_cachedValue = _prefix + _originalText;
		}
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
	 * @return The <em>old</em> value of the text cycler, including the prefix. This value can also be retrieved by the appropriate methods, however its computation is not cached.
	 */
	public String tick(){
		String val = toString();
		
		// Increment counter
		_currentTrimIndex = (_currentTrimIndex + 1) % (_originalText.length());
		
		return val;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _currentTrimIndex;
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
		if (_currentTrimIndex != other._currentTrimIndex) {
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
	
	/**
	 * Computes the current value of this text cycler instance.
	 * @return The current trimmed text value.
	 */
	@Override
	public String toString(){
		if (_cachedValue != null) {
			// No need to toString twice
			return _cachedValue;
		}

		StringBuilder display = new StringBuilder(_originalText.substring(_currentTrimIndex, Math.min(_currentTrimIndex + _trimLength, _originalText.length())));

		if (display.length() < _trimLength)
		{
			int add = _trimLength - display.length();
			display.append(_originalText.substring(0, Math.min(add, _originalText.length())));
		}

		// Add prefix if needed
		if(_prefix.length() > 0){
			int newLen = _trimLength - _prefix.length();
			Bukkit.getLogger().log(Level.INFO, "New length: " + newLen + ", existing display len: " + display.length() + "(val = '" + display.toString() + "')");
			display.replace(newLen, display.length(), "");
			display.insert(0, _prefix);
		}
		
		return display.toString();
	}

}
