package org.openhds.mobile.model.form;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

public class FormInstance implements Serializable {

    private static final long serialVersionUID = 1L;

	private String formName;
	private String filePath;
    private String fileName;
	private String uriString;
    private String formVersion;

    public String getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) {this.fileName = fileName;}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUriString() {
		return uriString;
	}

	public void setUriString(String uriString) {
		this.uriString = uriString;
	}

	public static List<File> toListOfFiles(List<FormInstance> formInstances) {
		ArrayList<File> fileList = new ArrayList<File>();

		for (FormInstance instance : formInstances) {
			fileList.add(new File(instance.getFilePath()));
		}
		return fileList;
	}

	public static List<Uri> toListOfUris(List<FormInstance> formInstances) {
		ArrayList<Uri> uriList = new ArrayList<Uri>();

		for (FormInstance instance : formInstances) {
			uriList.add(Uri.parse(instance.getUriString()));
		}
		return uriList;
	}
}
