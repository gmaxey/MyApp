// docker run --rm -v $(pwd):/scripts -w /scripts groovy:alpine groovy manifest-to-deployer.groovy '<JSON_HERE>'

@Grab('org.yaml:snakeyaml:1.33')
@Grab('org.codehaus.groovy:groovy-json')
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import groovy.json.JsonSlurper

// Check if JSON input is provided as a command-line argument
if (args.length == 0) {
    println "Error: Please provide a JSON input as a command-line argument."
    System.exit(1)
}

// Get the JSON input from the first command-line argument
def jsonInput = args[0]

// Parse the JSON input
def jsonSlurper = new JsonSlurper()
def parsedJson = jsonSlurper.parseText(jsonInput)

// Generate jobs dynamically from JSON
def jobs = parsedJson.collectEntries { entry ->
    [
        (entry.name): [
            uses: "gmaxey/reusableworkflows/MyAppDeploy.yaml",
            with: [
                artifactName: entry.name,
                artifactVersion: entry.version,
                environment: "pre-prod"
            ]
        ]
    ]
}

// Define the data structure with dynamic jobs
def data = [
    apiVersion: "automation.cloudbees.io/v1alpha1",
    kind: "workflow",
    name: "workflow",
    on: [
        workflow_dispatch: null,
        workflow_call: null 
    ],
    jobs: jobs
]

// Configure DumperOptions for better YAML formatting
def options = new DumperOptions()
options.setIndent(2)              // Set indentation to 2 spaces
options.setPrettyFlow(true)       // Enable pretty printing
options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) // Use block style for nested structures
options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN) // Use plain style for scalars

// Create YAML object with custom options
Yaml yaml = new Yaml(options)

// Convert data to YAML string
String yamlString = yaml.dump(data)

// Print the YAML
println yamlString

// Optionally, write to a file
new File("deployer.yaml").text = yamlString
