// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package oql.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.connectionbus.data.IDataRow;
import com.mendix.systemwideinterfaces.connectionbus.data.IDataTable;
import com.mendix.systemwideinterfaces.connectionbus.data.IDataTableSchema;
import com.mendix.systemwideinterfaces.connectionbus.requests.IParameterMap;
import com.mendix.systemwideinterfaces.connectionbus.requests.IRetrievalSchema;
import com.mendix.systemwideinterfaces.connectionbus.requests.types.IOQLTextGetRequest;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import com.opencsv.CSVWriter;
import oql.implementation.OQL;
import system.proxies.FileDocument;

public class ExportOQLToCSV extends CustomJavaAction<IMendixObject>
{
	private String statement;
	private IMendixObject returnEntity;
	private Boolean removeNewLinesFromValues;

	public ExportOQLToCSV(IContext context, String statement, IMendixObject returnEntity, Boolean removeNewLinesFromValues)
	{
		super(context);
		this.statement = statement;
		this.returnEntity = returnEntity;
		this.removeNewLinesFromValues = removeNewLinesFromValues;
	}

	@Override
	public IMendixObject executeAction() throws Exception
	{
		// BEGIN USER CODE
		final int PAGE_SIZE = 10000;
		
		ILogNode logger = Core.getLogger(this.getClass().getSimpleName());
		
		File tmpFile = File.createTempFile("Export", ".csv.zip");
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpFile));
		zos.putNextEntry(new ZipEntry(tmpFile.getName().replaceAll(".zip", "")));
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(zos));
		
		IMendixObject result = Core.instantiate(getContext(), this.returnEntity.getType());
		
		
		logger.debug("Executing query");
		
		int offset = 0;
		while(true) {
			logger.debug("Executing query offset " + offset);
			IContext context = getContext().createSudoClone();
			IDataTable results = Core.retrieveOQLDataTable(context, buildRequest(offset, PAGE_SIZE));
			IDataTableSchema tableSchema = results.getSchema();
			for (IDataRow row : results.getRows()) {
				String[] values = new String[tableSchema.getColumnCount()];
				for (int i = 0; i < tableSchema.getColumnCount(); i++) {
					Object value = row.getValue(getContext(), i);
					if (value == null) {
						values[i] = "";
					} else {
						values[i] = value.toString();
						if (this.removeNewLinesFromValues) {
							values[i] = values[i].replaceAll("\r", " ").replaceAll("\n", "");
						}
					}
				}
				writer.writeNext(values);
			}
			results.getRows().clear();
			if (results.getRowCount() != PAGE_SIZE) {
				break;
			}
			offset += PAGE_SIZE;
		}
		writer.close();
		result.setValue(getContext(), FileDocument.MemberNames.Name.toString(), tmpFile.getName());
		Core.storeFileDocumentContent(getContext(), result, new FileInputStream(tmpFile));
		tmpFile.delete();
		OQL.reset();
		
		return result;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public String toString()
	{
		return "ExportOQLToCSV";
	}

	// BEGIN EXTRA CODE
	private IOQLTextGetRequest buildRequest(int offset, int pagesize) {
		IOQLTextGetRequest request = Core.createOQLTextGetRequest();
		request.setQuery(statement);
		IParameterMap parameterMap = request.createParameterMap();
		IRetrievalSchema schema = Core.createRetrievalSchema();
		schema.setAmount(pagesize);
		schema.setOffset(offset);
		request.setRetrievalSchema(schema);
		for (Entry<String, Object> entry : OQL.getNextParameters().entrySet()) {
			parameterMap.put(entry.getKey(), entry.getValue());
		}
		request.setParameters(parameterMap);
		return request;
	}
	// END EXTRA CODE
}
