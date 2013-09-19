package corp.data.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import corp.data.CorpMetaData;
import corp.data.Gazetteer;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import corp.util.CounterTable;

public class CorpRelFeatureMetaDataAttribute extends CorpRelFeature {
	public enum InputType {
		Mentioner,
		Mentioned,
		Both
	}
	
	private Gazetteer gazetteer;
	private CorpMetaData metaData;
	private CorpMetaData.Attribute attribute;
	private CorpRelFeatureMetaDataAttribute.InputType inputType;
	private HashMap<String, Integer> attributeVocabulary = new HashMap<String, Integer>();
	private int minFeatureOccurrence;
	
	public CorpRelFeatureMetaDataAttribute(Gazetteer gazetteer, 
										   CorpMetaData metaData, 
										   CorpMetaData.Attribute attribute,
										   CorpRelFeatureMetaDataAttribute.InputType inputType,
										   int minFeatureOccurrence) {
		this.gazetteer = gazetteer;
		this.metaData = metaData;
		this.attribute = attribute;
		this.inputType = inputType;
		this.minFeatureOccurrence = minFeatureOccurrence;
	}
	
	@Override
	public void init(List<CorpRelDatum> data) {
		CounterTable attValues = new CounterTable();
		for (CorpRelDatum datum : data) {
			HashSet<String> datumAttValues = getDatumAttributeValues(datum);
			
			for (String attValue : datumAttValues) {
				attValues.incrementCount(attValue);
			}
		}
		
		attValues.removeCountsLessThan(this.minFeatureOccurrence);
		this.attributeVocabulary = attValues.buildIndex();
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		List<String> names = new ArrayList<String>(Collections.nCopies(this.attributeVocabulary.size(), (String)null));
		
		String namePrefix = "MetaDataAttribute_" + this.gazetteer.getName() + "_" + this.metaData.getName() + "_" + this.attribute + "_" + this.inputType + "_";
		for (Entry<String, Integer> entry : this.attributeVocabulary.entrySet())
			names.set(entry.getValue(), namePrefix + entry.getKey());
		existingNames.addAll(names);
		
		return existingNames;
	}
	
	@Override
	public List<Double> computeVector(CorpRelDatum datum,
			List<Double> existingVector) {
		HashSet<String> attValues = getDatumAttributeValues(datum);
		
		List<Double> featureValues = new ArrayList<Double>(Collections.nCopies(this.attributeVocabulary.size(), 0.0));
		for (String attValue : attValues) {
			if (this.attributeVocabulary.containsKey(attValue))
				featureValues.set(this.attributeVocabulary.get(attValue), 1.0);		
		}
		
		existingVector.addAll(featureValues);
		
		return existingVector;
	}
	
	@Override
	public CorpRelFeature clone() {
		return new CorpRelFeatureMetaDataAttribute(this.gazetteer, this.metaData, this.attribute, this.inputType, this.minFeatureOccurrence);
	}
	
	private HashSet<String> getMentionerAttributeValues(CorpRelDatum datum) {
		return getAttributeValuesForCorpStr(datum.getAuthorCorpName());
	}
	
	private HashSet<String> getMentionedAttributeValues(CorpRelDatum datum) {
		List<CorpDocumentTokenSpan> tokenSpans = datum.getOtherOrgTokenSpans();
		HashSet<String> attValues = new HashSet<String>();
		for (CorpDocumentTokenSpan tokenSpan : tokenSpans) 
			attValues.addAll(getAttributeValuesForCorpStr(tokenSpan.toString()));
		return attValues;
	}
	
	private HashSet<String> getMentionerMentionedAttributeValues(CorpRelDatum datum) {
		HashSet<String> mentionerAttValues = getMentionerAttributeValues(datum);
		HashSet<String> mentionedAttValues = getMentionedAttributeValues(datum);
		HashSet<String> attValues = new HashSet<String>();
		for (String mentionerAttValue : mentionerAttValues) {
			for (String mentionedAttValue : mentionedAttValues) {
				attValues.add(mentionerAttValue + "_" + mentionedAttValue);
			}
		}
		return attValues;
	}
	
	private HashSet<String> getDatumAttributeValues(CorpRelDatum datum) {
		if (this.inputType == CorpRelFeatureMetaDataAttribute.InputType.Mentioner) {
			return getMentionerAttributeValues(datum);
		} else if (this.inputType == CorpRelFeatureMetaDataAttribute.InputType.Mentioned) {
			return getMentionedAttributeValues(datum);
		} else if (this.inputType == CorpRelFeatureMetaDataAttribute.InputType.Both) {
			return getMentionerMentionedAttributeValues(datum);
		}
		return null;
	}
	
	private HashSet<String> getAttributeValuesForCorpStr(String corpStr) {
		List<String> ids = this.gazetteer.getIds(corpStr);
		HashSet<String> attributeValues = new HashSet<String>();
		
		if (ids == null)
			return attributeValues;
		
		for (String id : ids) {
			String attributeValue = this.metaData.getAttributeById(id, this.attribute);
			attributeValues.add(attributeValue);
		}
		
		return attributeValues;
	}
}
