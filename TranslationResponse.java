public class TranslationResponse 
{
	private int code;
	private String lang;
	private String[] text;
	private String origText;
	
	public void setCode(int code)
	{
		this.code = code;
	}
	public int getCode()
	{
		return code;
	}
	
	public void setLang(String lang)
	{
		this.lang = lang;
	}
	public String getLang()
	{
		return lang;
	}
	
	public void setText(String[] text)
	{
		this.text = text;
	}
	public String[] getText()
	{
		return text;
	}
	
	public void setOrigText(String text)
	{
		this.origText = text;
	}
	public String getOrigText()
	{
		return origText;
	}
}
