package ro.cs.tao.persistence.data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
///**
// * ProcessingComponent persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.processing_component")
//public class ProcessingComponent implements Serializable {
//
//    /**
//     * Processing component name column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_NAME_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Processing component label column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_LABEL_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Processing component version column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_VERSION_COLUMN_MAX_LENGTH = 50;
//
//    /**
//     * Processing component description column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_DESCRIPTION_COLUMN_MAX_LENGTH = 1024;
//
//    /**
//     * Processing component authors column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_AUTHORS_COLUMN_MAX_LENGTH = 50;
//
//    /**
//     * Processing component main tool file location column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_MAIN_TOOL_FILE_LOCATION_COLUMN_MAX_LENGTH = 512;
//
//    /**
//     * Processing component working directory column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_WORKING_DIRECTORY_COLUMN_MAX_LENGTH = 512;
//
//    /**
//     * Processing component template name column maximum length
//     */
//    private static final int PROCESSING_COMPONENT_TEMPLATE_NAME_COLUMN_MAX_LENGTH = 500;
//
//    /**
//     * Unique identifier
//     */
//    @Id
//    @SequenceGenerator(name = "processing_component_identifier", sequenceName = "tao.processing_component_id_seq", allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processing_component_identifier")
//    @Column(name = "id")
//    @NotNull
//    private Integer id;
//
//    /**
//     * Processing component name
//     */
//    @Column(name = "name")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_COMPONENT_NAME_COLUMN_MAX_LENGTH)
//    private String name;
//
//    /**
//     * Processing component label
//     */
//    @Column(name = "label")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_COMPONENT_LABEL_COLUMN_MAX_LENGTH)
//    private String label;
//
//    /**
//     * Processing component version
//     */
//    @Column(name = "version")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_COMPONENT_VERSION_COLUMN_MAX_LENGTH)
//    private String version;
//
//    /**
//     * Processing component description
//     */
//    @Column(name = "description")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_COMPONENT_DESCRIPTION_COLUMN_MAX_LENGTH)
//    private String description;
//
//    /**
//     * Processing component authors
//     */
//    @Column(name = "authors")
//    @Size(min = 1, max = PROCESSING_COMPONENT_AUTHORS_COLUMN_MAX_LENGTH)
//    private String authors;
//
//    /**
//     * Processing component copyright
//     */
//    @Column(name = "copyright")
//    @NotNull
//    private String copyright;
//
//    /**
//     * Processing component main tool file location
//     */
//    @Column(name = "main_tool_file_location")
//    @Size(min = 1, max = PROCESSING_COMPONENT_MAIN_TOOL_FILE_LOCATION_COLUMN_MAX_LENGTH)
//    private String mainToolFileLocation;
//
//    /**
//     * Processing component working directory
//     */
//    @Column(name = "working_directory")
//    @Size(min = 1, max = PROCESSING_COMPONENT_WORKING_DIRECTORY_COLUMN_MAX_LENGTH)
//    private String workingDirectory;
//
//    /**
//     * Processing component template type
//     */
//    @Column(name = "template_type_id")
//    @NotNull
//    private Integer templateType;
//
//    /**
//     * Processing component template name
//     */
//    @Column(name = "template_name")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_COMPONENT_TEMPLATE_NAME_COLUMN_MAX_LENGTH)
//    private String templateName;
//
//    /**
//     * The owner of this processing component
//     */
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "owner_user_id", nullable = true)
//    private User owner;
//
//    /**
//     * Processing component visibility
//     */
//    @Column(name = "visibility_id")
//    @NotNull
//    private Integer visibility;
//
//    /**
//     * The owner of this processing component
//     */
//    @OneToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "operation_id", nullable = false)
//    private ProcessingOperation processingOperation;
//
//    /**
//     * Flag that indicates if the processing component is multi thread or not
//     */
//    @Column(name = "multi_thread")
//    @NotNull
//    private Boolean multiThread;
//
//    /**
//     * Created date
//     */
//    @Column(name = "created")
//    @NotNull
//    private LocalDateTime createdDate;
//
//    /**
//     * Modified date
//     */
//    @Column(name = "modified")
//    private LocalDateTime modifiedDate;
//
//    /**
//     * Flag that indicates if the processing component is active or not
//     */
//    @Column(name = "active")
//    @NotNull
//    private Boolean active;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getLabel() {
//        return label;
//    }
//
//    public void setLabel(String label) {
//        this.label = label;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getAuthors() {
//        return authors;
//    }
//
//    public void setAuthors(String authors) {
//        this.authors = authors;
//    }
//
//    public String getCopyright() {
//        return copyright;
//    }
//
//    public void setCopyright(String copyright) {
//        this.copyright = copyright;
//    }
//
//    public String getMainToolFileLocation() {
//        return mainToolFileLocation;
//    }
//
//    public void setMainToolFileLocation(String mainToolFileLocation) {
//        this.mainToolFileLocation = mainToolFileLocation;
//    }
//
//    public String getWorkingDirectory() {
//        return workingDirectory;
//    }
//
//    public void setWorkingDirectory(String workingDirectory) {
//        this.workingDirectory = workingDirectory;
//    }
//
//    public Integer getTemplateType() {
//        return templateType;
//    }
//
//    public void setTemplateType(Integer templateType) {
//        this.templateType = templateType;
//    }
//
//    public String getTemplateName() {
//        return templateName;
//    }
//
//    public void setTemplateName(String templateName) {
//        this.templateName = templateName;
//    }
//
//    public User getOwner() {
//        return owner;
//    }
//
//    public void setOwner(User owner) {
//        this.owner = owner;
//    }
//
//    public Integer getVisibility() {
//        return visibility;
//    }
//
//    public void setVisibility(Integer visibility) {
//        this.visibility = visibility;
//    }
//
//    public ProcessingOperation getProcessingOperation() {
//        return processingOperation;
//    }
//
//    public void setProcessingOperation(ProcessingOperation processingOperation) {
//        this.processingOperation = processingOperation;
//    }
//
//    public Boolean getMultiThread() {
//        return multiThread;
//    }
//
//    public void setMultiThread(Boolean multiThread) {
//        this.multiThread = multiThread;
//    }
//
//    public LocalDateTime getCreatedDate() {
//        return createdDate;
//    }
//
//    public void setCreatedDate(LocalDateTime createdDate) {
//        this.createdDate = createdDate;
//    }
//
//    public LocalDateTime getModifiedDate() {
//        return modifiedDate;
//    }
//
//    public void setModifiedDate(LocalDateTime modifiedDate) {
//        this.modifiedDate = modifiedDate;
//    }
//
//    public Boolean getActive() {
//        return active;
//    }
//
//    public void setActive(Boolean active) {
//        this.active = active;
//    }
//}
