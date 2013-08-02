package com.archermind.filemanager.util;

import java.io.IOException;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;

public class XmlParser {
	private static final String TAG = "XmlParser";
	private Context mContext = null;

	public XmlParser(Context context) {
		mContext = context;
	}

	public static final String RESP_VALUE = "value";
	public static final String RESP_CODE = "Code";
	public static final String ERR_VALUE_MINUS_1 = "-1";
	public static final String ERR_VALUE_500 = "500";

	/*
	 * private boolean isErrCode(String tagName, String tagContent) {
	 * if((tagName.equals(RESP_VALUE) &&
	 * tagContent.trim().equals(ERR_VALUE_MINUS_1)) ||(tagName.equals(RESP_CODE)
	 * && tagContent.trim().equals(ERR_VALUE_500))) { return true; } return
	 * false; }
	 * 
	 * public boolean parserType(XmlPullParser parser, LinkedList<TypeItem>
	 * tiList) throws XmlPullParserException, IOException { String TagName =
	 * null; TypeItem ti = null; tiList.clear();
	 * 
	 * boolean bInResponse = false; for (;;) { switch (parser.next()) { case
	 * XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName();
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; ti = new
	 * TypeItem(); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if (bInResponse && isErrCode(TagName, TagContent)) { // return when error
	 * happens, list may be empty or have some items. return false; }
	 * 
	 * if (TagName.equals(TypeItem.ID_TAG) && null != ti) { int id =
	 * NumberUtils.parseInt(parser.getText()); if (-1 < id) ti.setId(id); break;
	 * }
	 * 
	 * if (TagName.equals(TypeItem.NAME_TAG) && null != ti) {
	 * ti.setName(parser.getText()); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE)) { bInResponse = false; if (
	 * null != ti ) { tiList.add(ti); ti = null; } break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * public boolean parserAllQuestion(XmlPullParser parser,
	 * LinkedList<QuestionItem> qiList) throws XmlPullParserException,
	 * IOException { String TagName = null; QuestionItem qi = null;
	 * qiList.clear();
	 * 
	 * boolean bInResponse = false; for ( ; ; ) { switch (parser.next()) { case
	 * XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName();
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; qi = new
	 * QuestionItem(); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if( bInResponse && isErrCode(TagName, TagContent) ) { return false; }
	 * 
	 * parseQuestion(TagName, TagContent, qi); break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE) ) { bInResponse = false; if
	 * (null != qi) { qiList.add(qi); qi = null; } break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * 
	 * 
	 * public boolean parserFriendQuestion(XmlPullParser parser,
	 * LinkedList<FriendQuestionItem> friendQuestionList) throws
	 * XmlPullParserException, IOException { String TagName = null;
	 * FriendQuestionItem fqi = null; LinkedList<FriendItem> friendList = null;
	 * QuestionItem qi = null;
	 * 
	 * friendQuestionList.clear();
	 * 
	 * boolean bInResponse = false; for ( ; ; ) { switch (parser.next()) { case
	 * XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName();
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; fqi = new
	 * FriendQuestionItem(); qi = new QuestionItem(); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if( bInResponse && isErrCode(TagName, TagContent) ) { return false; }
	 * 
	 * parseQuestion(TagName, TagContent, qi); if
	 * (TagName.equals(FriendQuestionItem.FRIENDS_TAG)) { TagContent.trim();
	 * friendList = new LinkedList<FriendItem>(); String[] numbers =
	 * StringUtils.split(TagContent, ","); for (int i = 0; i < numbers.length;
	 * i++) { FriendItem fi = new FriendItem(numbers[i], null);
	 * fi.valid(mContext.getContentResolver()); friendList.add(fi); } break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE) ) { bInResponse = false;
	 * 
	 * if(null != qi) { if (null != friendList) { fqi.setQuestion(qi);
	 * fqi.setFriends(friendList); friendQuestionList.add(fqi); }
	 * 
	 * fqi = null; friendList = null; qi = null; } break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * 
	 * 
	 * public boolean parserMyQuestion(XmlPullParser parser,
	 * LinkedList<QuestionItem> qiList) throws XmlPullParserException,
	 * IOException { String TagName = null; QuestionItem qi = null;
	 * qiList.clear();
	 * 
	 * boolean bInResponse = false; for ( ; ; ) { switch (parser.next()) { case
	 * XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName();
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; qi = new
	 * QuestionItem(); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if( bInResponse && isErrCode(TagName, TagContent) ) { return false; }
	 * parseQuestion(TagName, TagContent, qi); break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE) ) { bInResponse = false;
	 * if(null != qi) { qiList.add(qi); qi = null; } break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * public boolean parserSubject(XmlPullParser parser,
	 * LinkedList<SubjectItem> siList) throws XmlPullParserException,
	 * IOException { String TagName = null; SubjectItem subject = null;
	 * LinkedList<AnswerItem> answerList = null; siList.clear();
	 * 
	 * boolean bInResponse = false; for (;;) { switch (parser.next()) { case
	 * XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName();
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; break; }
	 * 
	 * if (SubjectItem.TOPIC.equals(TagName)) { subject = new SubjectItem();
	 * answerList = new LinkedList<AnswerItem>();
	 * 
	 * int id = NumberUtils.parseInt(parser.getAttributeValue(null,
	 * SubjectItem.ID)); if (-1 < id) subject.setId(id);
	 * 
	 * subject.setName(parser.getAttributeValue(null, SubjectItem.NAME));
	 * 
	 * int type = NumberUtils.parseInt(parser.getAttributeValue(null,
	 * SubjectItem.TYPE)); if (-1 < type) subject.setType(type); break; }
	 * 
	 * if (AnswerItem.ANSWER.equals(TagName) && null != answerList) { AnswerItem
	 * answer = new AnswerItem();
	 * 
	 * if (null != answerList) answerList.add(answer);
	 * 
	 * int id = NumberUtils.parseInt(parser.getAttributeValue(null,
	 * AnswerItem.ID)); if (-1 < id) answer.setId(id);
	 * 
	 * answer.setName(parser.getAttributeValue(null, AnswerItem.NAME));
	 * 
	 * int value = NumberUtils.parseInt(parser.getAttributeValue(null,
	 * AnswerItem.VALUE)); if (-1 < value) answer.setValue(value); break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if( bInResponse && isErrCode(TagName, TagContent) ) { return false; }
	 * break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE) ) { bInResponse = false;
	 * break; } if (SubjectItem.TOPIC.equals(parser.getName()) && null !=
	 * answerList && null != subject) { subject.setAnswers(answerList);
	 * siList.add(subject); subject = null; answerList = null; break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * 
	 * 
	 * 
	 * public boolean parserVoteResult(XmlPullParser parser, ScoreItem
	 * scoreItem) throws XmlPullParserException, IOException { String TagName =
	 * null; boolean bInResponse = false; for (;;) { switch (parser.next()) {
	 * case XmlPullParser.START_DOCUMENT: break;
	 * 
	 * case XmlPullParser.START_TAG: TagName = parser.getName(); LogUtils.i(TAG,
	 * "tag name: " + TagName);
	 * 
	 * if (TagName.equals(TypeItem.RESPONSE)) { bInResponse = true; break; } if
	 * (ScoreItem.VOTE_SCORE.equals(TagName)) { int id =
	 * NumberUtils.parseInt(parser.getAttributeValue(null, AnswerItem.ID)); }
	 * 
	 * if (ScoreItem.TOTAL_SCORE.equals(TagName)) { break; }
	 * 
	 * break;
	 * 
	 * case XmlPullParser.TEXT: String TagContent = parser.getText(); if
	 * (TextUtils.isEmpty(TagContent) || TagContent.contains("\n")) { break; }
	 * 
	 * if( bInResponse && isErrCode(TagName, TagContent) ) { return false; }
	 * break;
	 * 
	 * case XmlPullParser.END_TAG: if
	 * (parser.getName().equals(TypeItem.RESPONSE) ) { bInResponse = false;
	 * break; } break;
	 * 
	 * case XmlPullParser.END_DOCUMENT: return true; } } }
	 * 
	 * private void parseQuestion(String TagName, String TagContent,
	 * QuestionItem qi) { if (TagName.equals(QuestionItem.ID_TAG) && null != qi)
	 * { int id = NumberUtils.parseInt(TagContent); if (-1 < id) qi.setId(id);
	 * return; }
	 * 
	 * if (TagName.equals(QuestionItem.NAME_TAG) && null != qi) {
	 * qi.setName(TagContent); return; }
	 * 
	 * 
	 * if (TagName.equals(QuestionItem.TIME_TAG) && null != qi) { int index =
	 * TagContent.indexOf(' '); String date = TagContent.substring(0, index);
	 * 
	 * qi.setTime(date); return; }
	 * 
	 * if (TagName.equals(QuestionItem.POINTS_TAG) && null != qi) { int
	 * pintValue = NumberUtils.parseInt(TagContent); if (-1 < pintValue)
	 * qi.setPoints(pintValue); return; }
	 * 
	 * 
	 * if (TagName.equals(QuestionItem.FROM_TAG) && null != qi) { if
	 * (TextUtils.isEmpty(TagContent))
	 * qi.setFrom(mContext.getString(R.string.from)); else
	 * qi.setFrom(mContext.getString(R.string.from) + " " + TagContent);
	 * 
	 * return; }
	 * 
	 * if (TagName.equals(TypeItem.ID_TAG) && null != qi) { int ClassId =
	 * NumberUtils.parseInt(TagContent); if (-1 < ClassId)
	 * qi.setTypeId(ClassId); return; } }
	 */
}