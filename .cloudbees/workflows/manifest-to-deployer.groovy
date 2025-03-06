@Grab('org.yaml:snakeyaml:1.33')
@Grab('org.codehaus.groovy:groovy-json')
import org.yaml.snakeyaml.Yaml
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
        uses: entry.uses,
        with: entry.with
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
        build: [
            steps: steps
        ]
    ]
]

// Create YAML object
Yaml yaml = new Yaml()

// Convert data to YAML string
String yamlString = yaml.dump(data)

// Print the YAML
println yamlString

// Optionally, write to a file
new File("workflow.yaml").text = yamlString
