package me.pagekite.glen3b.library.bukkit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A class which rotates through text to keep it within a specific length limit.
 * All {@link CharSequence} methods are performed regarding the trimmed text.
 * @author Glen Husman
 */
public class TextCycler extends BukkitRunnable implements CharSequence {

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
			int spaceCt = _trimLength - ((_originalText.length() + _prefix.length()) % _trimLength);
			String spacedString = StringUtils.rightPad(_originalText, _originalText.length() + spaceCt);
			List<String> entrySet = new ArrayList<String>();
			entrySet.add(_prefix + _originalText.substring(0, _trimLength - _prefix.length()));
			for(int i = 0; i < spacedString.length(); i++){
				String added;
				if(i + _trimLength - _prefix.length() < spacedString.length()){
					added = _prefix + spacedString.substring(i, i + _trimLength - _prefix.length());
				}else{
					added = _prefix + spacedString.substring(i) + Utilities.Strings.getString((i + _trimLength - _prefix.length()) - spacedString.length(), ' ');
				}
				if(!entrySet.contains(added)){
					entrySet.add(added);
				}
			}
			for(int i = 0; i < _trimLength; i++){
				String str = _prefix + Utilities.Strings.getString(_trimLength - i, ' ') + _originalText.substring(0, Math.max(i - _prefix.length(), 0));
				if(!entrySet.contains(str)){
					entrySet.add(str);
				}
			}
			Iterator<String> listIter = entrySet.iterator();
			boolean foundWhitespace = false;
			while(listIter.hasNext()){
				String value = listIter.next();
				if(StringUtils.isWhitespace(value.substring(_prefix.length())) && (foundWhitespace || value.length() != _trimLength)){
					listIter.remove();
					foundWhitespace = true;
				}
			}
			_trimPossibilities = entrySet.toArray(new String[0]);
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

	/**
	 * Ticks the text cycler.
	 */
	@Override
	public final void run() {
		tick();
	}

}
