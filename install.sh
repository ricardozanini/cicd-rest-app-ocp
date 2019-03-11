#!/bin/sh
# TODO: turn this into an Ansilble role. :)

# create the applications, here we need three envs to promote the images between them
oc new-project sample-rest-app-dev
oc new-project sample-rest-app-stg
oc new-project sample-rest-app-prd

oc policy add-role-to-user edit system:serviceaccount:cicd:jenkins -n sample-rest-app-dev
oc policy add-role-to-user edit system:serviceaccount:cicd:jenkins -n sample-rest-app-stg
oc policy add-role-to-user edit system:serviceaccount:cicd:jenkins -n sample-rest-app-prd