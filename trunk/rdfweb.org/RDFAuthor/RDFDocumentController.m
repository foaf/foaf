#import "RDFDocumentController.h"

@implementation RDFDocumentController

- (IBAction)openUrl:(id)sender
{
    //[urlField setStringValue:@""];
    [openUrlPanel makeKeyAndOrderFront: self];
    [NSApp runModalForWindow: openUrlPanel];
}

- (IBAction)urlSubmitted:(id)sender
{
    NSString *urlString;
    NSURL *url;
    id result;
    
    [openUrlPanel orderOut: self];
    [NSApp stopModal];

    urlString = [urlComboBox stringValue];
    NSLog(urlString);
    url = [NSURL URLWithString: urlString];
    NSLog(@"Url is: %@", url);
    result = [self openDocumentWithContentsOfURL: url display: YES];
    NSLog(@"Document is: %@", result);
}

- (IBAction)userCancelled:(id)sender
{
    [openUrlPanel orderOut: self];
    [NSApp stopModal];
}

- (id)openDocumentWithContentsOfURL:(NSURL *)anURL display:(BOOL)flag
{
    id doc = [self makeDocumentWithContentsOfURL: anURL ofType: @"RDF/XML Document"];
    
    if (doc)
    {
        [doc makeWindowControllers];
        
        [self addDocument: doc];
        
        if (flag) [doc showWindows];
    }

    return doc;
}

@end
