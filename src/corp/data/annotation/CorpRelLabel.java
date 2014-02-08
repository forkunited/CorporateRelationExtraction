package corp.data.annotation;

/**
 * 
 * CorpRelLabel represents corporate relationship type labels that are nodes 
 * within the relationship type taxonomy.  Most of these labels are given by
 * abbreviated versions of the names from the technical report documentation.
 * It's easiest to keep these abbreviations as long as they are used in the
 * corporate relationship annotation data.
 * 
 * The comments next to the label names indicate the corresponding names within 
 * the tech report pdf.
 * 
 * @author Bill McDowell
 *
 */
public enum CorpRelLabel {
	SelfRef, // Self-reference
	OCorp, // Other-corporation
	Family, // Family
	Parent, // Parent
	Sub, // Subsidiary
	Division, // Division
	Sister, // Sister
	Merger, // Merger
	Aqu,  // Acquisition-source
	Target, // Acquisition-target
	Merge, // Acquisition
	Legal, // Legal
	Lawsuit, // Lawsuit
	Alliance, // Alliance
	Agreement, // Agreement
	Partner, // Partner
	Cust, // Customer
	Suply, // Supplier
	Sup, // Other (under Supplier)
	LegalS, // Legal-services
	IB, // Investment-banking
	Cons, // Consulting
	Audit, // Auditing
	NewHire, // New-hire
	Compete, // Competitor
	News, // Media-source
	Finance, // Financial-investment
	New, // [Nothing... this is not used]
	NonCorp, // Non-corporation
	US, // US-federal-government
	State, // US-state-government
	nonUS, // Non-US-government
	Ind, // Industry-group
	Rating, // Bond-rater
	University, // University
	Generic, // Generic
	DontKnow, // Unknown
	Error, // Error
	Person, // Person
	Place, // Place
	BadParse, // Bad-parse
	Other, // Other (under Merger and Error)
	Garbage // Garbage
}
