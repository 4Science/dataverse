/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author gdurand
 */
public class DataCitation {

    private String authors;
    private String title;
    private Date citationDate;
    private GlobalId persistentId;
    private String version;
    private String UNF;
    private String distributors;

    public DataCitation(String authors, String title, Date citationDate, GlobalId persistentId, String version, String UNF, String distributors) {
        this.authors = authors;
        this.title = title;
        this.citationDate = citationDate;
        this.persistentId = persistentId;
        this.version = version;
        this.UNF = UNF;
        this.distributors = distributors;
    }

    public DataCitation(DatasetVersion dsv) {
        // authors (or producer)
        authors = dsv.getAuthorsStr(false);
        if (StringUtils.isEmpty(authors)) {
            authors = dsv.getDatasetProducersString();
        }

        // citation date
        if (!dsv.getDataset().isHarvested()) {
            citationDate = dsv.getCitationDate();
            if (citationDate == null) {
                if (dsv.getDataset().getPublicationDate() != null) {
                    citationDate = dsv.getDataset().getPublicationDate();
                } else { // for drafts
                    citationDate = new Date();
                }
            }
        } else {
            try {
                citationDate = new SimpleDateFormat("yyyy").parse(dsv.getDistributionDate());
            } catch (ParseException ex) {
                // ignore
            }
        }

        // title
        title = dsv.getTitle();

        // The Global Identifier: 
        // It is always part of the citation for the local datasets; 
        // And for *some* harvested datasets. 
        if (!dsv.getDataset().isHarvested()
                || HarvestingDataverseConfig.HARVEST_STYLE_VDC.equals(dsv.getDataset().getOwner().getHarvestingDataverseConfig().getHarvestStyle())
                || HarvestingDataverseConfig.HARVEST_STYLE_ICPSR.equals(dsv.getDataset().getOwner().getHarvestingDataverseConfig().getHarvestStyle())
                || HarvestingDataverseConfig.HARVEST_STYLE_DATAVERSE.equals(dsv.getDataset().getOwner().getHarvestingDataverseConfig().getHarvestStyle())) {
            if (!StringUtils.isEmpty(dsv.getDataset().getIdentifier())) {
                persistentId = new GlobalId(dsv.getDataset().getGlobalId());
            }
        }

        // distributors
        if (!dsv.getDataset().isHarvested()) {
            distributors = dsv.getRootDataverseNameforCitation();
        } else {
            distributors = dsv.getDistributorName();
            if (!StringUtils.isEmpty(distributors)) {
                distributors += " [distributor]";
            }
        }

        // version
        if (!dsv.getDataset().isHarvested()) {
            if (dsv.isDraft()) {
                version = "DRAFT VERSION";
            } else if (dsv.getVersionNumber() != null) {
                version = "V" + dsv.getVersionNumber();
                if (dsv.isDeaccessioned()) {
                    version += ", DEACCESSIONED VERSION";
                }
            }
        }
        
        // UNF
        UNF = dsv.getUNF();

    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCitationDate() {
        return citationDate;
    }

    public void setCitationDate(Date citationDate) {
        this.citationDate = citationDate;
    }

    public GlobalId getPersistentId() {
        return persistentId;
    }

    public void setPersistentId(GlobalId persistentId) {
        this.persistentId = persistentId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUNF() {
        return UNF;
    }

    public void setUNF(String UNF) {
        this.UNF = UNF;
    }

    public String getDistributors() {
        return distributors;
    }

    public void setDistributors(String distributors) {
        this.distributors = distributors;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean html) {

        List<String> citationList = new ArrayList<>();
        
        // first add comma separated parts
        addNonEmptyStringToList(citationList, formatString(authors, html));
        addNonEmptyStringToList(citationList, formatNonEmptyDate(citationDate,"yyyy"));
        addNonEmptyStringToList(citationList, formatString(title, html, "\""));
        addNonEmptyStringToList(citationList, formatURL(persistentId.toURL(), html));
        addNonEmptyStringToList(citationList, formatString(distributors, html));
        addNonEmptyStringToList(citationList, version);
        StringBuilder citation = new StringBuilder(StringUtils.join(citationList, ", "));
        
        // append UNF
        if (!StringUtils.isEmpty(UNF)) {
            citation.append( " [").append(UNF).append("]");
        }
        
        return citation.toString();
    }
    

    // helper methods
    private void addNonEmptyStringToList(List<String> list, String value) {
        if (!StringUtils.isEmpty(value)) {
            list.add(value);
        }
    }
    
    private String formatString(String value, boolean escapeHtml) {
        return formatString(value, escapeHtml, "");
    }
     
    
    private String formatString(String value, boolean escapeHtml, String wrapper) {
        if (!StringUtils.isEmpty(value)) {
            return new StringBuilder(wrapper)
                    .append(escapeHtml ? StringEscapeUtils.escapeHtml(value) : value)
                    .append(wrapper).toString();
        }
        return null;
    }  
    
    private String formatNonEmptyDate(Date date, String format) {
        return date == null ? null : new SimpleDateFormat(format).format(date);
    }
    
    private String formatURL(URL value, boolean html) {
        if (value ==null) {
            return null;
        }
        
        if (html) {
            return "<a href=\"" + value.toString() + "\" target=\"_blank\">" + value.toString() + "</a>";
        } else {
            return value.toString();
        }
            
    }   
}
