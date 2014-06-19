package corp.data.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import corp.data.CorpMetaData;
import corp.data.Gazetteer;
import corp.data.annotation.CorpDocumentTokenSpan;
import corp.data.annotation.CorpRelDatum;
import ark.util.CounterTable;
import corp.util.SerializationUtil;
import corp.util.StringUtil;

/**
 * For organization mention m and gazetteer G, 
 * CorpRelFeatureMetaDataAttribute computes either vector:
 * 
 * <1(v_1 \in a(A(m))), 1(v_2 \in a(A(m))), ... , 1(v_n \in a(A(m)))>
 * 
 * Or:
 * 
 * <1(v_1 \in a(O(m))), 1(v_2 \in a(O(m))), ... , 1(v_n \in a(O(m)))>
 * 
 * Or:
 * 
 * <1(v_1,v_1) \in a(A(m))xa(O(m))), 1((v_1,v_2) \in a(A(m))xa(O(m))), ... , 1((v_n,v_n) \in a(A(m))xa(O(m)))>
 * 
 * Where O(m) is the mentioned organization, A(m) is the authoring 
 * corporation, a(s) returns a set of meta-data attributes for 
 * organization s, and v_i is a possible value for attribute a.  Whether 
 * A(m) or O(m) is used is determined by the "input type".
 * 
 * @author Bill McDowell
 *
 */
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
	private Map<String, Integer> attributeVocabulary = new HashMap<String, Integer>();
	private int minFeatureOccurrence;
	private StringUtil.StringCollectionTransform attributeTransformFn;
	
	public CorpRelFeatureMetaDataAttribute(Gazetteer gazetteer, 
			   CorpMetaData metaData, 
			   CorpMetaData.Attribute attribute,
			   CorpRelFeatureMetaDataAttribute.InputType inputType,
			   int minFeatureOccurrence,
			   StringUtil.StringCollectionTransform attributeTransformFn) {
		this.gazetteer = gazetteer;
		this.metaData = metaData;
		this.attribute = attribute;
		this.inputType = inputType;
		this.minFeatureOccurrence = minFeatureOccurrence;
		this.attributeTransformFn = attributeTransformFn;
	}
	
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
		this.attributeTransformFn = null;
	}
	
	private String getNamePrefix() {
		String transformName = (this.attributeTransformFn == null) ? "" : this.attributeTransformFn.toString() + "_";
		return "MetaDataAttribute_" + this.gazetteer.getName() + "_" + this.metaData.getName() + "_" + transformName + this.attribute + "_" + this.inputType + "_MinF" + this.minFeatureOccurrence + "_";
	}
	
	@Override
	public void init(List<CorpRelDatum> data) {
		CounterTable<String> attValues = new CounterTable<String>();
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
	public void init(String initStr) {
		this.attributeVocabulary = new HashMap<String, Integer>();
		Map<String, String> strVocabulary = SerializationUtil.deserializeArguments(initStr);
		for (Entry<String, String> entry : strVocabulary.entrySet())
			this.attributeVocabulary.put(entry.getKey(), Integer.parseInt(entry.getValue()));
	}
	
	@Override
	public Map<String, Double> computeMapNoInit(CorpRelDatum datum) {
		String namePrefix = getNamePrefix();
		HashSet<String> attValues = getDatumAttributeValues(datum);
		Map<String, Double> map = new HashMap<String, Double>(attValues.size());
		for (String attValue : attValues) {
			map.put(namePrefix + attValue, 1.0);
		}
		return map;
	}
	
	@Override
	public List<String> getNames(List<String> existingNames) {
		List<String> names = new ArrayList<String>(Collections.nCopies(this.attributeVocabulary.size(), (String)null));
		
		String namePrefix = getNamePrefix();
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
		return new CorpRelFeatureMetaDataAttribute(this.gazetteer, this.metaData, this.attribute, this.inputType, this.minFeatureOccurrence, this.attributeTransformFn);
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
			List<String> attributeValue = this.metaData.getAttributeById(id, this.attribute);
			if (this.attributeTransformFn == null) {
				for (String value : attributeValue) {
					attributeValues.add(StringUtil.clean(value));
				}
			} else { 
				for (String value : attributeValue) {
					Collection<String> transformedValues = this.attributeTransformFn.transform(value);
					for (String transformedValue : transformedValues)
						attributeValues.add(StringUtil.clean(transformedValue));
				}
			}
		}
		
		return attributeValues;
	}
	
	@Override
	public String toString(boolean withInit) {
		String str = "MetaDataAttribute(gazetteer=" + this.gazetteer.getName() + "Gazetteer, " +
								 "metaData=" + this.metaData.getName() + "MetaData, " +
								 "attribute=" + this.attribute + ", " +
								 "inputType=" + this.inputType + ", " +
								 "minFeatureOccurrence=" + this.minFeatureOccurrence + ", " +
								 "attributeTransformFn=" + this.attributeTransformFn.toString() +
								 ")";
		
		if (withInit) {
			str += "\t" + SerializationUtil.serializeArguments(this.attributeVocabulary);
		}
		
		return str;
	}
}
