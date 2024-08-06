set dotenv-load

default:
    @just --list

namespace:= "play-deals-backend"
serviceAccountName:= "play-deals-backend-deployer"
tag := "$(git rev-parse --short HEAD)"
imageRepo := "ghcr.io/psuzn/play-deals-backend"

dev-run:
  ./gradlew backend:run

build-push-image imageTag=tag:
    #!/usr/bin/env sh
    tags="{{tag}},latest"
    ./gradlew jib  \
        -Djib.to.image={{imageRepo}} \
        -Djib.to.tags=$tags \
        -Djib.to.auth.username=$DOCKER_USER \
        -Djib.to.auth.password=$DOCKER_PASSWORD

    echo Pushed tags $tags

ns-create:
    kubectl create namespace {{ namespace }}

ns-delete:
    kubectl delete namespace {{ namespace }}

# Runs helm upgrade
helm-upgrade imageTag=tag:
    #!/usr/bin/env sh
    echo "Deploying {{imageRepo}}:{{imageTag}} to $(kubectl config current-context)"
    helm upgrade play-deals-backend --create-namespace \
        --install --namespace {{namespace}} ./helm/backend \
        --set db.host=$DB_HOST \
        --set db.port=$DB_PORT \
        --set db.username=$DB_USERNAME \
        --set db.password=$DB_PASSWORD \
        --set db.name=$DB_NAME \
        --set backgroundTask.dashboard=$DASHBOARD \
        --set backgroundTask.dashboardUser=$DASHBOARD_USER \
        --set backgroundTask.dashboardPass=$DASHBOARD_PASS \
        --set firebaseAdminAuthCredential=$FIREBASE_ADMIN_AUTH_CREDENTIALS \
        --set forexApiKey=$FOREX_API_KEY \
        --set image.tag={{imageTag}} \
        --set image.repository={{imageRepo}} \
        --set namespace={{namespace}}

# Uninstalls the helm chart
helm-delete:
    helm uninstall -n {{namespace}} play-deals-backend

# creates a service account and token for a deployer
helm-create-deployer:
    echo "Creating deployer service account to $(kubectl config current-context)"
    helm install play-deals-backend-deployer ./helm/service-account \
        --namespace {{namespace}}  --create-namespace \
        --set serviceAccountName={{serviceAccountName}} \
        --set namespace={{namespace}}

# deletes the deployer service account
helm-delete-deployer:
    echo "Deleting deployer service account to $(kubectl config current-context)"
    helm uninstall play-deals-backend-deployer --namespace {{namespace}}

# gets the deployer kubeconfig
deployer-cubeconfig:
    #!/usr/bin/env sh
    CLUSTER_NAME=$(kubectl config current-context)
    SECRET_NAME="sa-{{ serviceAccountName }}-token"
    SA_TOKEN=$(kubectl get secret $SECRET_NAME -n {{namespace}}  -o jsonpath='{.data.token}' | base64 -D)
    CA_DATA=$(kubectl get secret $SECRET_NAME -n {{namespace}} -o jsonpath='{.data.ca\.crt}')
    K8S_ENDPOINT=$(kubectl config view -o jsonpath="{.clusters[?(@.name=='${CLUSTER_NAME}')].cluster.server}")
    echo "
    apiVersion: v1
    kind: Config
    clusters:
      - name: default-cluster
        cluster:
          certificate-authority-data: ${CA_DATA}
          server: ${K8S_ENDPOINT}
    contexts:
    - name: default-context
      context:
        cluster: default-cluster
        namespace: {{namespace}}
        user: default-user
    current-context: default-context
    users:
    - name: default-user
      user:
        token: ${SA_TOKEN}
    "


