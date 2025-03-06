@Grab('org.yaml:snakeyaml:1.33')
@Grab('org.codehaus.groovy:groovy-json')
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import groovy.json.JsonSlurper

// Define the JSON input
def jsonInput = '[{"name":"MyAppAPIs","version":"2.0.5-0.0.17","workflow":"deploy.yaml"},{"name":"MyAppFE","version":"3.0.3-0.0.14","workflow":"deploy.yaml"},{"name":"MyAppBE","version":"2.2.0-0.0.21","workflow":"deploy.yaml"}]'

// Parse the JSON input
def jsonSlurper = new JsonSlurper()
def parsedJson = jsonSlurper.parseText(jsonInput)

// Generate steps dynamically from JSON
def steps = parsedJson.collect { entry ->
    [
        name: entry.name,
        uses: entry.workflow,
        with: [
            name: entry.name,
            version: entry.version,
            environment: "pre-prod"
        ]
    ]
}

// Define the data structure with dynamic steps
def data = [
    apiVersion: "automation.cloudbees.io/v1alpha1",
    kind: "workflow",
    name: "workflow",
    on: [
        workflow_dispatch: null
    ],
    jobs: [
        deploy: [
            steps: steps
        ]
    ]
]

// Configure DumperOptions for better YAML formatting
def options = new DumperOptions()
options.setIndent(2)              // Set indentation to 2 spaces
options.setPrettyFlow(true)       // Enable pretty printing
options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) // Use block style for nested structures

// Create YAML object with custom options
Yaml yaml = new Yaml(options)

// Convert data to YAML string
String yamlString = yaml.dump(data)

// Print the YAML
println yamlString

// Optionally, write to a file
new File("deployer.yaml").text = yamlString
